package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.infrastructure.aspect.GenerateIds;
import com.arassec.artivact.application.infrastructure.aspect.RestrictResult;
import com.arassec.artivact.application.infrastructure.aspect.TranslateResult;
import com.arassec.artivact.application.port.in.collection.*;
import com.arassec.artivact.application.port.in.configuration.LoadPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.LoadMenuUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.CollectionExportRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.exchange.CollectionExportInfo;
import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.misc.ExchangeDefinitions;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.COLLECTION_EXCHANGE_SUFFIX;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.ZIP_FILE_SUFFIX;

/**
 * Service for collection export handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ManageCollectionExportService
        implements LoadCollectionExportUseCase,
        SaveCollectionExportUseCase,
        DeleteCollectionExportUseCase,
        BuildCollectionExportFileUseCase,
        ReadCollectionExportFileUseCase,
        LoadCollectionExportCoverPictureUseCase,
        DeleteCollectionExportCoverPictureUseCase,
        SaveCollectionExportCoverPictureUseCase,
        CreateCollectionExportInfosUseCase {

    /**
     * The application's file repository.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * Repository for collection export access.
     */
    private final CollectionExportRepository collectionExportRepository;

    private final LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;

    private final LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;

    private final LoadMenuUseCase loadMenuUseCase;

    /**
     * The application's standard {@link JsonMapper}.
     */
    private final JsonMapper jsonMapper;

    private final ExportCollectionUseCase exportCollectionUseCase;

    private final UseProjectDirsUseCase useProjectDirsUseCase;

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    /**
     * Returns all collection exports available to the current user.
     *
     * @return All available collection exports.
     */
    @TranslateResult
    @RestrictResult
    @Override
    public List<CollectionExport> loadAllRestricted() {
        return loadAll();
    }

    /**
     * Returns all available collection exports.
     *
     * @return All available collection exports.
     */
    @TranslateResult
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void saveSortOrder(List<CollectionExport> collectionExports) {
        collectionExportRepository.saveSortOrder(collectionExports);
    }

    /**
     * Deletes the collection export with the given ID.
     *
     * @param id The ID of the collection export to delete.
     */
    @Override
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
    @Override
    public InputStream readExportFile(String id) {
        Path exportFile = getExportFile(id);
        if (fileRepository.exists(exportFile)) {
            try {
                return Files.newInputStream(exportFile);
            } catch (IOException e) {
                throw new ArtivactException("Cannot read export file for export with ID: " + id, e);
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
    @Override
    public synchronized void buildExportFile(String id) {
        runBackgroundOperationUseCase.execute("collectionExport", "export", progressMonitor -> {
            CollectionExport collectionExport = collectionExportRepository.findById(id).orElseThrow();
            Path exportedFile = exportCollectionUseCase.exportCollection(collectionExport,
                    loadMenuUseCase.loadMenu(collectionExport.getSourceId()),
                    loadPropertiesConfigurationUseCase.loadPropertiesConfiguration(),
                    loadTagsConfigurationUseCase.loadTagsConfiguration());
            log.info("Build export: {}", exportedFile);

            collectionExportRepository.save(collectionExport);

            progressMonitor.updateProgress(1, 1);
        });
    }

    /**
     * Saves the provided image as cover picture of the collection export.
     *
     * @param id               The collection export's ID.
     * @param originalFilename The image's original name.
     * @param inputStream      The input stream providing the image data.
     */
    @Override
    public void saveCoverPicture(String id, String originalFilename, InputStream inputStream) {
        String fileExtension = fileRepository.getExtension(originalFilename).orElseThrow();

        CollectionExport collectionExport = collectionExportRepository.findById(id).orElseThrow();

        Path targetDir = useProjectDirsUseCase.getExportsDir();
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
    @Override
    public byte[] loadCoverPicture(String id) {
        CollectionExport collectionExport = collectionExportRepository.findById(id).orElseThrow();
        if (!StringUtils.hasText(collectionExport.getCoverPictureExtension())) {
            throw new ArtivactException("No cover picture available for collection export: " + id);
        }

        Path coverPicture = useProjectDirsUseCase.getExportsDir().resolve(id + "." + collectionExport.getCoverPictureExtension());

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
    @Override
    public void deleteCoverPicture(String id) {
        CollectionExport collectionExport = collectionExportRepository.findById(id).orElseThrow();
        if (StringUtils.hasText(collectionExport.getCoverPictureExtension())) {
            Path coverPicture = useProjectDirsUseCase.getExportsDir()
                    .resolve(id + "." + collectionExport.getCoverPictureExtension());
            if (fileRepository.exists(coverPicture)) {
                fileRepository.delete(coverPicture);
            }
            collectionExport.setCoverPictureExtension(null);
            collectionExportRepository.save(collectionExport);
        }
    }

    /**
     * Loads the overview of all available collection exports and writes it to the given output stream.
     *
     * @param targetOutputStream The output stream to write the export to.
     * @param collectionExports  The collection exports to convert and return.
     */
    @Override
    public void createCollectionExportInfos(OutputStream targetOutputStream, List<CollectionExport> collectionExports) {
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
            final ZipOutputStream zipOutputStream = new ZipOutputStream(targetOutputStream);

            ZipEntry zipEntry = new ZipEntry(ExchangeDefinitions.COLLECTION_EXPORT_OVERVIEWS_JSON_FILE);

            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(jsonMapper.writeValueAsBytes(collectionExportInfos));

            getFileRepository().list(useProjectDirsUseCase.getExportsDir()).stream()
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
     * Returns the path to the export file of the collection export with the given ID.
     *
     * @param id The collection export's ID.
     * @return Path to the export file.
     */
    private Path getExportFile(String id) {
        return useProjectDirsUseCase.getExportsDir()
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
