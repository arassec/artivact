package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.core.model.exchange.CollectionExportInfo;
import com.arassec.artivact.core.model.item.ImageSize;
import com.arassec.artivact.core.repository.CollectionExportRepository;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.aspect.GenerateIds;
import com.arassec.artivact.domain.aspect.RestrictResult;
import com.arassec.artivact.domain.aspect.TranslateResult;
import com.arassec.artivact.domain.exchange.ArtivactExporter;
import com.arassec.artivact.domain.exchange.ArtivactImporter;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.COLLECTION_EXCHANGE_SUFFIX;
import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.ZIP_FILE_SUFFIX;

/**
 * Service for collection export handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CollectionExportService extends BaseFileService {

    /**
     * Filename of the content-export-overviews file.
     */
    public static final String COLLECTION_EXPORT_OVERVIEWS_FILE = "artivact.collection-export-overviews" + ZIP_FILE_SUFFIX;

    /**
     * Filename of the content-export-overviews file.
     */
    public static final String COLLECTION_EXPORT_OVERVIEWS_JSON_FILE = "artivact.collection-export-overviews.json";

    /**
     * The application's file repository.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * Repository for collection export access.
     */
    private final CollectionExportRepository collectionExportRepository;

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The menu service.
     */
    private final MenuService menuService;

    /**
     * Exporter for Artivact.
     */
    private final ArtivactExporter artivactExporter;

    /**
     * Importer for Artivact.
     */
    private final ArtivactImporter artivactImporter;

    /**
     * Provider for basic project data.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * The application's standard {@link ObjectMapper}.
     */
    private final ObjectMapper objectMapper;

    /**
     * The service's progress monitor for long-running tasks.
     */
    @Getter
    private ProgressMonitor progressMonitor;

    /**
     * Executor service for background tasks.
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    /**
     * Returns all collection exports available to the current user.
     *
     * @return All available collection exports.
     */
    @TranslateResult
    @RestrictResult
    public List<CollectionExport> loadAllRestricted() {
        return loadAll();
    }

    /**
     * Returns all available collection exports.
     *
     * @return All available collection exports.
     */
    @TranslateResult
    public List<CollectionExport> loadAll() {
        List<CollectionExport> collectionExports = collectionExportRepository.findAll();
        collectionExports.forEach(this::addAdditionalInformation);
        return collectionExports;
    }

    /**
     * Loads a single collection export.
     *
     * @param id The collection export's ID.
     * @return The collection export.
     */
    @TranslateResult
    public CollectionExport load(String id) {
        CollectionExport collectionExport = collectionExportRepository.findById(id).orElseThrow();
        addAdditionalInformation(collectionExport);
        return collectionExport;
    }

    /**
     * Creates a new collection export.
     *
     * @param collectionExport The collection export to create.
     */
    @GenerateIds
    public void save(CollectionExport collectionExport) {
        if (collectionExport == null
                || collectionExport.getId() == null
                || !StringUtils.hasText(collectionExport.getSourceId())
                || !StringUtils.hasText(collectionExport.getTitle().getValue())) {
            throw new ArtivactException("Cannot save a collection export without required parameters: " + collectionExport);
        }
        collectionExportRepository.save(collectionExport);
    }

    /**
     * Saves the sort order of the supplied collection exports.
     *
     * @param collectionExports Collection exports to save.
     */
    public void saveSortOrder(List<CollectionExport> collectionExports) {
        collectionExportRepository.saveSortOrder(collectionExports);
    }

    /**
     * Deletes the collection export with the given ID.
     *
     * @param id The ID of the collection export to delete.
     */
    public void delete(String id) {
        Path exportFile = getExportFile(id);
        if (fileRepository.exists(exportFile)) {
            fileRepository.delete(exportFile);
        }
        deleteCoverPicture(id);
        collectionExportRepository.delete(id);
    }

    /**
     * Returns a collection export's file as stream.
     *
     * @param id The collection export's ID.
     * @return An {@link InputStream} containing the export file.
     */
    public InputStream readExportFile(String id) {
        Path exportFile = getExportFile(id);
        if (fileRepository.exists(exportFile)) {
            try {
                return Files.newInputStream(exportFile);
            } catch (IOException e) {
                throw new ArtivactException("Cannot read export file for export with ID: " + id);
            }
        }
        throw new ArtivactException("Cannot read export file for export with ID: " + id);
    }

    /**
     * (Re-)Builds a collection export's package file.
     *
     * @param id The collection export's ID.
     */
    @GenerateIds
    public synchronized void build(String id) {
        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "buildCollectionExport");

        executorService.submit(() -> {
            try {
                CollectionExport collectionExport = collectionExportRepository.findById(id).orElseThrow();
                Path exportedFile = artivactExporter.exportCollection(collectionExport, menuService.findMenu(collectionExport.getSourceId()),
                        configurationService.loadPropertiesConfiguration(), configurationService.loadTagsConfiguration());
                log.info("Build export: {}", exportedFile);

                collectionExportRepository.save(collectionExport);

                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("buildCollectionExportFailed", e);
                log.error("Error during collection export!", e);
            }
        });
    }

    /**
     * Saves the provided image as cover picture of the collection export.
     *
     * @param id               The collection export's ID.
     * @param originalFilename The image's original name.
     * @param inputStream      The input stream providing the image data.
     */
    public void saveCoverPicture(String id, String originalFilename, InputStream inputStream) {
        String fileExtension = getExtension(originalFilename).orElseThrow();

        CollectionExport collectionExport = collectionExportRepository.findById(id).orElseThrow();

        Path targetDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR);
        fileRepository.createDirIfRequired(targetDir);

        Path coverPicture = targetDir.resolve(id + "." + fileExtension);
        fileRepository.scaleImage(inputStream, coverPicture, fileExtension, ImageSize.PAGE_TITLE.getWidth());
        collectionExport.setCoverPictureExtension(fileExtension);

        collectionExportRepository.save(collectionExport);
    }

    /**
     * Loads a collection export's cover picture.
     *
     * @param id The ID of the collection export.
     * @return The image as byte array.
     */
    public byte[] loadCoverPicture(String id) {
        CollectionExport collectionExport = collectionExportRepository.findById(id).orElseThrow();
        if (!StringUtils.hasText(collectionExport.getCoverPictureExtension())) {
            throw new ArtivactException("No cover picture available for collection export: " + id);
        }

        Path coverPicture = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR)
                .resolve(id + "." + collectionExport.getCoverPictureExtension());

        try {
            return Files.readAllBytes(coverPicture);
        } catch (IOException e) {
            throw new ArtivactException("Could not load image!", e);
        }
    }

    /**
     * Deletes the cover picture of a collection export.
     *
     * @param id The collection export's ID.
     */
    public void deleteCoverPicture(String id) {
        CollectionExport collectionExport = collectionExportRepository.findById(id).orElseThrow();
        if (StringUtils.hasText(collectionExport.getCoverPictureExtension())) {
            Path coverPicture = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR)
                    .resolve(id + "." + collectionExport.getCoverPictureExtension());
            if (fileRepository.exists(coverPicture)) {
                fileRepository.delete(coverPicture);
            }
            collectionExport.setCoverPictureExtension(null);
            collectionExportRepository.save(collectionExport);
        }
    }

    /**
     * Imports previously created collection exports to the application by reading from the exported ZIP file.
     *
     * @param file The exported ZIP file.
     */
    public synchronized void importCollection(MultipartFile file) {
        importCollection(file, false);
    }

    /**
     * Imports previously created collection exports to the application by saving the exported ZIP file.
     *
     * @param file The exported ZIP file.
     */
    public synchronized void importCollectionForDistribution(MultipartFile file) {
        importCollection(file, true);
    }

    /**
     * Loads the overview of all available collection exports and writes it to the given output stream.
     *
     * @param outputStream      The output stream to write the export to.
     * @param collectionExports The collection exports to convert and return.
     */
    public void createCollectionExportInfos(OutputStream outputStream, List<CollectionExport> collectionExports) {
        List<CollectionExportInfo> collectionExportInfos = collectionExports.stream()
                .filter(CollectionExport::isFilePresent)
                .map(collectionExport -> CollectionExportInfo.builder()
                        .id(collectionExport.getId())
                        .title(collectionExport.getTitle())
                        .description(collectionExport.getDescription())
                        .size(collectionExport.getFileSize())
                        .lastModified(collectionExport.getFileLastModified())
                        .build())
                .toList();

        try {
            final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

            ZipEntry zipEntry = new ZipEntry(COLLECTION_EXPORT_OVERVIEWS_JSON_FILE);

            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(objectMapper.writeValueAsBytes(collectionExportInfos));

            Path exportsDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR);
            getFileRepository().list(exportsDir).stream()
                    .filter(path -> !path.getFileName().toString().endsWith("zip"))
                    .filter(path -> !fileRepository.isDir(path))
                    .forEach(path -> {
                        File file = new File(path.toString());
                        ZipEntry mediaZipEntry = new ZipEntry(file.getName());

                        try (var inputStream = new FileInputStream(file)) {
                            zipOutputStream.putNextEntry(mediaZipEntry);
                            byte[] bytes = new byte[1024];
                            int length;
                            while ((length = inputStream.read(bytes)) >= 0) {
                                zipOutputStream.write(bytes, 0, length);
                            }
                        } catch (IOException e) {
                            throw new ArtivactException("Exception while reading and streaming data!", e);
                        }
                    });

            zipOutputStream.close();

        } catch (IOException e) {
            throw new ArtivactException("Could not create item export file!", e);
        }
    }

    /**
     * Imports a previously created collection export.
     *
     * @param file                The export file.
     * @param onlyForDistribution Set to {@code true}, if only the file should be imported, {@code false} if also the
     *                            content (menus, pages, items) should be imported.
     */
    private synchronized void importCollection(MultipartFile file, boolean onlyForDistribution) {
        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "importCollection");

        Path importFileZip = saveImportFile(projectDataProvider.getProjectRoot(), file);

        executorService.submit(() -> {
            try {
                Path existingCollectionExportFile = projectDataProvider.getProjectRoot()
                        .resolve(ProjectDataProvider.EXPORT_DIR)
                        .resolve(importFileZip.getFileName().toString());
                if (fileRepository.exists(existingCollectionExportFile)) {
                    fileRepository.delete(existingCollectionExportFile);
                }
                CollectionExport collectionExport = artivactImporter.importCollection(importFileZip, onlyForDistribution);
                collectionExportRepository.save(collectionExport);
                progressMonitor = null;
            } catch (Exception e) {
                log.error("Error during content import!", e);
                progressMonitor.updateProgress("importContentFailed", e);
            }
        });
    }

    /**
     * Returns the path to the export file of the collection export with the given ID.
     *
     * @param id The collection export's ID.
     * @return Path to the export file.
     */
    private Path getExportFile(String id) {
        return projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR)
                .resolve(id + COLLECTION_EXCHANGE_SUFFIX + ZIP_FILE_SUFFIX);
    }

    /**
     * Adds additional information, e.g., about the export file' size and last modification, to the provided collection
     * export.
     *
     * @param collectionExport The {@link CollectionExport} to add additional information to.
     */
    private void addAdditionalInformation(CollectionExport collectionExport) {
        Path exportFile = getExportFile(collectionExport.getId());
        if (fileRepository.exists(exportFile)) {
            collectionExport.setFilePresent(true);
            collectionExport.setFileLastModified(fileRepository.lastModified(exportFile).toEpochMilli());
            collectionExport.setFileSize(fileRepository.size(exportFile));
        } else {
            collectionExport.setFilePresent(false);
        }

        if (collectionExport.getContent() == null) {
            collectionExport.setContent(new TranslatableString(""));
        }
    }

}
