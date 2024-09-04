package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.configuration.ExchangeConfiguration;
import com.arassec.artivact.core.model.export.ContentExport;
import com.arassec.artivact.core.model.item.ImageSize;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.aspect.TranslateResult;
import com.arassec.artivact.domain.export.ArtivactExporter;
import com.arassec.artivact.domain.export.json.model.ContentExportFile;
import com.arassec.artivact.domain.export.model.ExportParams;
import com.arassec.artivact.domain.export.model.ExportType;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Service for handling item exports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService extends BaseFileService {

    /**
     * Filename of the content-export-overviews file.
     */
    public static final String CONTENT_EXPORT_OVERVIEWS_FILE = "artivact.content-export-overviews.zip";

    /**
     * Filename of the content-export-overviews file.
     */
    public static final String CONTENT_EXPORT_OVERVIEWS_JSON_FILE = "artivact.content-export-overviews.json";

    /**
     * File suffix for exported content.
     */
    public static final String CONTENT_EXPORT_SUFFIX = ".artivact.content.";

    /**
     * File suffix for zipped exported items.
     */
    public static final String ITEM_EXPORT_SUFFIX = ".artivact.item.zip";

    /**
     * File name for exported items JSON data files.
     */
    public static final String ITEM_DATA_FILE = "artivact.item";

    /**
     * Label suffix for progress monitoring.
     */
    private static final String PROGRESS_SUFFIX_PACKAGING = "packaging";

    /**
     * The service for configuration handling.
     */
    private final ConfigurationService configurationService;

    /**
     * The service for item handling.
     */
    private final ItemService itemService;

    /**
     * The application's {@link ProjectDataProvider}.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * List of available exporters, injected by Spring.
     */
    private final List<ArtivactExporter> exporters;

    /**
     * The application's {@link FileRepository}.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * The application's object mapper.
     */
    @Getter
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
     * Saves an export's cover picture.
     *
     * @param menuId           The menu the export is based on.
     * @param originalFilename The original filename of the cover picture.
     * @param inputStream      The input stream containing the picture.
     */
    public void saveCoverPicture(String menuId, String originalFilename, InputStream inputStream) {
        String fileExtension = getExtension(originalFilename).orElseThrow();
        String coverPictureFilename = menuId + "." + fileExtension;

        Path exportsDir = projectDataProvider.getProjectRoot().resolve("exports");

        getFileRepository().createDirIfRequired(exportsDir);
        getFileRepository().scaleImage(inputStream, exportsDir.resolve(coverPictureFilename), fileExtension, ImageSize.PAGE_TITLE.getWidth());
    }

    /**
     * Creates a menu's export ZIP file.
     *
     * @param menuId The menu's ID.
     */
    public synchronized void exportContent(ExportParams params, String menuId) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "exportContent");

        // The menu chosen by the user to export:
        List<Menu> flattenedMenus = configurationService.loadTranslatedMenus();
        flattenedMenus.addAll(flattenedMenus.stream()
                .flatMap(existingMenu -> existingMenu.getMenuEntries().stream())
                .toList());
        Menu menu = flattenedMenus.stream()
                .filter(Objects::nonNull)
                .filter(existingMenu -> existingMenu.getId().equals(menuId))
                .findFirst()
                .orElseThrow();

        String exportName = menu.getId() + CONTENT_EXPORT_SUFFIX + params.getExportType().name().toLowerCase();
        params.setExportDir(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR));
        params.setContentExportDir(params.getExportDir().resolve(exportName));
        params.setContentExportFile(params.getExportDir().resolve(exportName + ".zip"));

        executorService.submit(() -> {
            try {
                if (fileRepository.exists(params.getContentExportFile())) {
                    fileRepository.delete(params.getContentExportFile());
                }

                if (fileRepository.exists(params.getContentExportDir())) {
                    fileRepository.delete(params.getContentExportDir());
                }

                fileRepository.createDirIfRequired(params.getContentExportDir());

                exporters.stream()
                        .filter(exporter -> exporter.supports().equals(params.getExportType()))
                        .forEach(exporter -> exporter.export(params, menu, progressMonitor));

                // Copy a cover picture if one exists:
                Optional<Path> coverPictureOptional = fileRepository.list(params.getExportDir()).stream()
                        .filter(file -> !fileRepository.isDir(file))
                        .filter(file -> file.getFileName().toString().startsWith(menu.getId()))
                        .findFirst();

                if (coverPictureOptional.isPresent()) {
                    Path coverPicture = coverPictureOptional.get();
                    fileRepository.copy(coverPicture, params.getContentExportDir().resolve(coverPicture.getFileName()));
                }

                if (params.isZipResults()) {
                    ZipUtil.pack(params.getContentExportDir().toAbsolutePath().toFile(),
                            params.getContentExportFile().toAbsolutePath().toFile());
                    fileRepository.delete(params.getContentExportDir().toAbsolutePath());
                }

                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("exportContentFailed", e);
                log.error("Error during content export!", e);
            }
        });
    }

    /**
     * Loads details about available content exports.
     *
     * @return List of {@link ContentExport}s.
     */
    @TranslateResult
    public List<ContentExport> loadContentExports() {
        return fileRepository.list(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR)).stream()
                .filter(path -> path.getFileName().toString().contains(CONTENT_EXPORT_SUFFIX))
                .map(this::createContentExport)
                .toList();
    }

    /**
     * Returns the filename of a content export with the specified values.
     *
     * @param menuId     The menu ID the export is based on.
     * @param exportType The type of the export.
     * @return The export file.
     */
    public Path getContentExportFile(String menuId, ExportType exportType) {
        return projectDataProvider.getProjectRoot()
                .resolve(ProjectDataProvider.EXPORT_DIR)
                .resolve(menuId + CONTENT_EXPORT_SUFFIX + exportType.name().toLowerCase() + FileRepository.ZIP_FILE_SUFFIX);
    }

    /**
     * Loads the given content export and writes it to the given output stream.
     *
     * @param menuId       The ID of the menu providing the export.
     * @param outputStream The output stream to write the export to.
     */
    public void loadContentExport(String menuId, ExportType exportType, OutputStream outputStream) {
        Path exportFile = getContentExportFile(menuId, exportType);
        if (fileRepository.exists(exportFile)) {
            fileRepository.copy(exportFile, outputStream);
        } else {
            throw new ArtivactException("No export file available for menu " + menuId + " and type " + exportType + "!");
        }
    }

    /**
     * Loads the overview of all available content exports and writes it to the given output stream.
     *
     * @param outputStream The output stream to write the export to.
     */
    public void loadContentExportOverviews(OutputStream outputStream) {
        List<ContentExport> contentExports = loadContentExports();

        try {
            final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

            ZipEntry zipEntry = new ZipEntry(CONTENT_EXPORT_OVERVIEWS_JSON_FILE);

            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(objectMapper.writeValueAsBytes(contentExports));

            Path exportsDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR);
            getFileRepository().list(exportsDir).stream()
                    .filter(path -> !path.getFileName().toString().endsWith("zip"))
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
     * Deletes a content export with the given menu ID.
     *
     * @param menuId The menu ID the export is based on.
     */
    public void deleteContentExport(String menuId) {
        Path exportDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR);
        fileRepository.list(exportDir).forEach(export -> {
            if (export.getFileName().toString().startsWith(menuId)) {
                fileRepository.delete(export);
            }
        });
    }

    /**
     * Creates a {@link ContentExport} instance from the given path, provided the path points to a content export
     * directory or file.
     *
     * @param path Path to the content export.
     * @return A new {@link ContentExport} instance with details about the export.
     */
    private ContentExport createContentExport(Path path) {
        ContentExport contentExport = new ContentExport();

        String exportType = "";
        if (path.getFileName().toString().contains(ArtivactExporter.CONTENT_EXPORT_FILE_SUFFIX_JSON)) {
            exportType = "JSON";
        }

        contentExport.setId(path.getFileName().toString().split("\\.")[0]);
        contentExport.setLastModified(fileRepository.lastModified(path).toEpochMilli());
        contentExport.setSize(fileRepository.size(path));
        contentExport.setZipped(path.getFileName().toString().endsWith(FileRepository.ZIP_FILE_SUFFIX));
        contentExport.setExportType(exportType);

        try {
            ContentExportFile contentExportFile = null;

            if (Files.isDirectory(path)) {
                contentExportFile = objectMapper.readValue(path
                        .resolve(ArtivactExporter.CONTENT_EXPORT_FILE_SUFFIX_JSON).toFile(), ContentExportFile.class);
            } else {
                try (ZipFile zipFile = new ZipFile(path.toFile())) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (entry.getName().equals(ArtivactExporter.CONTENT_EXPORT_FILE_SUFFIX_JSON)) {
                            InputStream stream = zipFile.getInputStream(entry);
                            contentExportFile = objectMapper.readValue(stream.readAllBytes(), ContentExportFile.class);
                        }
                    }
                }
            }

            if (contentExportFile != null) {
                contentExport.setTitle(contentExportFile.getTitle());
                contentExport.setDescription(contentExportFile.getDescription());
            }

        } catch (IOException e) {
            throw new ArtivactException("Could not read export content file!", e);
        }

        return contentExport;
    }

    /**
     * Creates an item's export ZIP file.
     *
     * @param itemId       The item's ID.
     * @param outputStream The output stream to write the export to.
     */
    public void createItemExportFile(String itemId, OutputStream outputStream) {
        Item item = itemService.loadTranslated(itemId);
        item.setMediaCreationContent(null);

        List<String> mediaFiles = itemService.getMediaFiles(itemId);

        try {
            final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

            ZipEntry zipEntry = new ZipEntry(ITEM_DATA_FILE + FileRepository.JSON_FILE_SUFFIX);

            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(objectMapper.writeValueAsBytes(item));

            zipMediaFiles(zipOutputStream, mediaFiles);
        } catch (IOException e) {
            throw new ArtivactException("Could not create item export file!", e);
        }
    }

    /**
     * Exports the item with the given ID and uploads it to a remote application instance configured in the exchange
     * configuration.
     *
     * @param itemId The item's ID.
     */
    public synchronized void exportItemToRemoteInstance(String itemId) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), PROGRESS_SUFFIX_PACKAGING);

        ExchangeConfiguration exchangeConfiguration = configurationService.loadExchangeConfiguration();
        String remoteServer = exchangeConfiguration.getRemoteServer();
        String apiToken = exchangeConfiguration.getApiToken();

        executorService.submit(() -> {
            exportItemToRemoteInstance(itemId, progressMonitor, remoteServer, apiToken);
            if (progressMonitor.getException() == null) {
                progressMonitor = null;
            }
        });
    }

    /**
     * Exports all items and uploads them to a remote application instance configured in the exchange configuration.
     */
    public synchronized void exportItemsToRemoteInstance() {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        List<String> itemIdsForRemoteSync = itemService.getItemIdsForRemoteSync();

        log.debug("Found {} items to upload to remote instance.", itemIdsForRemoteSync.size());

        progressMonitor = new ProgressMonitor(getClass(), PROGRESS_SUFFIX_PACKAGING);
        progressMonitor.updateProgress(0, itemIdsForRemoteSync.size());

        ExchangeConfiguration exchangeConfiguration = configurationService.loadExchangeConfiguration();
        String remoteServer = exchangeConfiguration.getRemoteServer();
        String apiToken = exchangeConfiguration.getApiToken();

        executorService.submit(() -> {
            itemIdsForRemoteSync.forEach(itemId -> {
                if (progressMonitor.getException() == null) {
                    log.debug("Processing item {}", itemId);
                    exportItemToRemoteInstance(itemId, progressMonitor, remoteServer, apiToken);
                    progressMonitor.updateProgress(progressMonitor.getCurrentAmount() + 1, itemIdsForRemoteSync.size());
                }
            });
            if (progressMonitor.getException() == null) {
                progressMonitor = null;
            }
        });
    }

    /**
     * Exports the item with the given ID to the supplied remote instance.
     *
     * @param itemId          The ID of the item to upload.
     * @param progressMonitor The progress monitor, which is updated during upload.
     * @param remoteServer    The remote instance to upload the item to.
     * @param apiToken        The API token for remote authentication and authorization.
     */
    private void exportItemToRemoteInstance(String itemId, final ProgressMonitor progressMonitor, String remoteServer, String apiToken) {

        if (!StringUtils.hasText(remoteServer)) {
            progressMonitor.updateProgress("configMissing", new ArtivactException("Remote server configuration missing!"));
            return;
        }
        if (!remoteServer.endsWith("/")) {
            remoteServer += "/";
        }

        if (!StringUtils.hasText(apiToken)) {
            progressMonitor.updateProgress("configMissing", new ArtivactException("API token configuration missing!"));
            return;
        }

        progressMonitor.updateLabelKey(PROGRESS_SUFFIX_PACKAGING);

        File exportFile = projectDataProvider.getProjectRoot()
                .resolve(ProjectDataProvider.TEMP_DIR)
                .resolve(itemId + ITEM_EXPORT_SUFFIX)
                .toAbsolutePath()
                .toFile();

        try (FileOutputStream fileOutputStream = new FileOutputStream(exportFile)) {
            createItemExportFile(itemId, fileOutputStream);
        } catch (IOException e) {
            progressMonitor.updateProgress("exportFileCreationFailed", e);
            log.error("Could not create export file to upload item to remote server!", e);
            return;
        }

        progressMonitor.updateLabelKey("uploading");

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(remoteServer + "api/import/remote/item/" + apiToken);

            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.STRICT)
                    .addPart("file", new FileBody(exportFile))
                    .build();
            httpPost.setEntity(entity);

            httpclient.execute(httpPost, response -> {
                if (response.getCode() != 200) {
                    progressMonitor.updateProgress("uploadFailed",
                            new ArtivactException("HTTP result code: " + response.getCode()));
                    log.error("Could not upload item file to remote server: HTTP result code {}, File '{}'", response.getCode(), exportFile);
                } else {
                    Item item = itemService.load(itemId).orElseThrow();
                    item.setSyncVersion(item.getVersion() + 1); // Saving the item will increase its version by one!
                    itemService.save(item);
                }
                return response;
            });

        } catch (Exception e) {
            progressMonitor.updateProgress("uploadFailed", e);
            log.error("Could not upload item file to remote server!", e);
        }
    }

}
