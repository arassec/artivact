package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.configuration.ExchangeConfiguration;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.exchange.StandardExportInfo;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.aspect.TranslateResult;
import com.arassec.artivact.domain.exchange.ArtivactExporter;
import com.arassec.artivact.domain.exchange.ExchangeProcessor;
import com.arassec.artivact.domain.exchange.model.ExchangeMainData;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for handling item exports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExportService extends BaseFileService implements ExchangeProcessor {

    /**
     * Filename of the content-export-overviews file.
     */
    public static final String CONTENT_EXPORT_OVERVIEWS_FILE = "artivact.content-export-overviews" + ZIP_FILE_SUFFIX;

    /**
     * Filename of the content-export-overviews file.
     */
    public static final String CONTENT_EXPORT_OVERVIEWS_JSON_FILE = "artivact.content-export-overviews.json";

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
     * Artivact exporter, injected by Spring.
     */
    private final ArtivactExporter artivactExporter;

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
     * Creates a menu's export ZIP file.
     *
     * @param menuId The menu's ID.
     */
    public synchronized void exportMenu(ExportConfiguration configuration, String menuId) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "exportContent");

        // The menu chosen by the user to export:
        List<Menu> flattenedMenus = configurationService.loadTranslatedRestrictedMenus();
        flattenedMenus.addAll(flattenedMenus.stream()
                .flatMap(existingMenu -> existingMenu.getMenuEntries().stream())
                .toList());
        Menu menu = flattenedMenus.stream()
                .filter(Objects::nonNull)
                .filter(existingMenu -> existingMenu.getId().equals(menuId))
                .findFirst()
                .orElseThrow();

        executorService.submit(() -> {
            try {
                Path exportedFile = artivactExporter.exportMenu(configuration, menu);
                log.info("Created export: {}", exportedFile);
                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("exportContentFailed", e);
                log.error("Error during content export!", e);
            }
        });
    }

    /**
     * Creates an item's export ZIP file.
     *
     * @param itemId The item's ID.
     */
    public Path exportItem(String itemId) {
        Item item = itemService.loadTranslated(itemId);
        return artivactExporter.exportItem(item);
    }

    /**
     * Copies a created export to an output stream and deletes it afterward.
     *
     * @param export       The export.
     * @param outputStream The target stream.
     */
    public void copyExportAndDelete(Path export, OutputStream outputStream) {
        fileRepository.copy(export, outputStream);
        fileRepository.delete(export);
    }

    /**
     * Exports the current property configuration and returns the export result as String.
     *
     * @return The exported property configuration.
     */
    public String exportPropertiesConfiguration() {
        return fileRepository.read(artivactExporter.exportPropertiesConfiguration());
    }

    /**
     * Exports the current tags configuration and returns the result as String.
     *
     * @return The exported tags configuration.
     */
    public String exportTagsConfiguration() {
        return fileRepository.read(artivactExporter.exportTagsConfiguration());
    }

    /**
     * Loads details about available content exports.
     *
     * @return List of {@link StandardExportInfo}s.
     */
    @TranslateResult
    public List<StandardExportInfo> loadContentExports() {
        return fileRepository.list(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR)).stream()
                .filter(path -> path.getFileName().toString().contains(CONTENT_EXCHANGE_SUFFIX + ZIP_FILE_SUFFIX))
                .map(this::createContentExport)
                .toList();
    }

    /**
     * Returns the filename of a content export with the specified values.
     *
     * @param menuId The menu ID the export is based on.
     * @return The export file.
     */
    public Path getContentExportFile(String menuId) {
        return projectDataProvider.getProjectRoot()
                .resolve(ProjectDataProvider.EXPORT_DIR)
                .resolve(menuId + CONTENT_EXCHANGE_SUFFIX + ZIP_FILE_SUFFIX);
    }

    /**
     * Loads the given content export and writes it to the given output stream.
     *
     * @param menuId       The ID of the menu providing the export.
     * @param outputStream The output stream to write the export to.
     */
    public void loadContentExport(String menuId, OutputStream outputStream) {
        Path exportFile = getContentExportFile(menuId);
        if (fileRepository.exists(exportFile)) {
            fileRepository.copy(exportFile, outputStream);
        } else {
            throw new ArtivactException("No export file available for menu " + menuId + "!");
        }
    }

    /**
     * Loads the overview of all available content exports and writes it to the given output stream.
     *
     * @param outputStream The output stream to write the export to.
     */
    public void loadContentExportOverviews(OutputStream outputStream) {
        List<StandardExportInfo> standardExportInfos = loadContentExports();

        try {
            final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

            ZipEntry zipEntry = new ZipEntry(CONTENT_EXPORT_OVERVIEWS_JSON_FILE);

            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(objectMapper.writeValueAsBytes(standardExportInfos));

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
     * @param exportSourceId The ID of the object the export is based on.
     */
    public void deleteExport(String exportSourceId) {
        Path exportDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR);
        fileRepository.list(exportDir).forEach(export -> {
            if (export.getFileName().toString().startsWith(exportSourceId)) {
                fileRepository.delete(export);
            }
        });
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
     * Exports all modified items and uploads them to a remote application instance configured in the exchange configuration.
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

        Path exportFile = exportItem(itemId);

        progressMonitor.updateLabelKey("uploading");

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(remoteServer + "api/import/remote/item/" + apiToken);

            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.STRICT)
                    .addPart("file", new FileBody(exportFile.toFile()))
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

                    fileRepository.delete(exportFile);
                }
                return response;
            });

        } catch (Exception e) {
            progressMonitor.updateProgress("uploadFailed", e);
            log.error("Could not upload item file to remote server!", e);
        }
    }

    /**
     * Creates a {@link StandardExportInfo} instance from the given path, provided the path points to a content export
     * directory or file.
     *
     * @param path Path to the content export.
     * @return A new {@link StandardExportInfo} instance with details about the export.
     */
    private StandardExportInfo createContentExport(Path path) {
        StandardExportInfo standardExportInfo = new StandardExportInfo();

        standardExportInfo.setId(path.getFileName().toString().split("\\.")[0]);
        standardExportInfo.setLastModified(fileRepository.lastModified(path).toEpochMilli());
        standardExportInfo.setSize(fileRepository.size(path));

        ExchangeMainData exchangeMainData = artivactExporter.readExchangeMainData(path);

        if (exchangeMainData != null) {
            standardExportInfo.setTitle(exchangeMainData.getTitle());
            standardExportInfo.setDescription(exchangeMainData.getDescription());
        }

        return standardExportInfo;
    }

}
