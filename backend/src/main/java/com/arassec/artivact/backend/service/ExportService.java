package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.api.BaseController;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.misc.ProgressMonitor;
import com.arassec.artivact.backend.service.misc.ProjectDataProvider;
import com.arassec.artivact.backend.service.model.configuration.ExchangeConfiguration;
import com.arassec.artivact.backend.service.model.item.Item;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
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
public class ExportService extends BaseController {

    /**
     * The service for configuration handling.
     */
    private final ConfigurationService configurationService;

    /**
     * The service for item handling.
     */
    private final ItemService itemService;

    /**
     * Provider for project data.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * The application's object mapper.
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
     * Creates an item's export ZIP file.
     *
     * @param itemId The item's ID.
     * @return The item's JSON representation and its media files in a {@link StreamingResponseBody}.
     */
    public StreamingResponseBody createItemExportFile(String itemId) {
        Item item = itemService.loadTranslated(itemId);
        item.setMediaCreationContent(null);

        List<String> mediaFiles = itemService.getMediaFiles(itemId);

        return out -> {
            final ZipOutputStream zipOutputStream = new ZipOutputStream(out);

            ZipEntry zipEntry = new ZipEntry("artivact.item.json");
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(objectMapper.writeValueAsBytes(item));

            zipMediaFiles(zipOutputStream, mediaFiles);
        };
    }

    /**
     * Exports the item with the given ID and uploads it to a remote application instance configured in the exchange
     * configuration.
     *
     * @param itemId The item's ID.
     * @return The progress monitor tracking the upload.
     */
    public synchronized ProgressMonitor syncItemUp(String itemId) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return progressMonitor;
        }

        progressMonitor = new ProgressMonitor(getClass(), "packaging");

        executorService.submit(() -> {
            File exportFile = projectDataProvider.getProjectRoot()
                    .resolve(ProjectDataProvider.TEMP_DIR)
                    .resolve(itemId + ".artivact.item.zip")
                    .toAbsolutePath()
                    .toFile();

            try (FileOutputStream fileOutputStream = new FileOutputStream(exportFile)) {
                StreamingResponseBody streamingResponseBody = createItemExportFile(itemId);
                streamingResponseBody.writeTo(fileOutputStream);
            } catch (IOException e) {
                progressMonitor.updateProgress("exportFileCreationFailed", e);
                log.error("Could not create export file to upload item to remote server!", e);
            }

            ExchangeConfiguration exchangeConfiguration = configurationService.loadExchangeConfiguration();
            String remoteServer = exchangeConfiguration.getRemoteServer();
            if (!StringUtils.hasText(remoteServer)) {
                progressMonitor.updateProgress("configMissing", new ArtivactException("Configuration missing!"));
            }
            if (!remoteServer.endsWith("/")) {
                remoteServer += "/";
            }

            progressMonitor.updateLabelKey("uploading");

            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(remoteServer
                        + "api/exchange/sync/item/import/"
                        + exchangeConfiguration.getApiToken());

                HttpEntity entity = MultipartEntityBuilder.create()
                        .setMode(HttpMultipartMode.STRICT)
                        .addPart("file", new FileBody(exportFile))
                        .build();
                httpPost.setEntity(entity);

                httpclient.execute(httpPost, response -> {
                    if (response.getCode() != 200) {
                        progressMonitor.updateProgress("uploadFailed",
                                new ArtivactException("HTTP result code: " + response.getCode()));
                        log.error("Could not upload item file to remote server: HTTP result code " + response.getCode());
                        return progressMonitor;
                    }
                    return response;
                });

                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("uploadFailed", e);
                log.error("Could not upload item file to remote server!", e);
            }
        });

        return progressMonitor;
    }

}
