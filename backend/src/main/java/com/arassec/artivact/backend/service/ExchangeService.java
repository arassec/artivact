package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.account.Account;
import com.arassec.artivact.backend.service.model.configuration.ExchangeConfiguration;
import com.arassec.artivact.backend.service.model.item.Item;
import com.arassec.artivact.backend.service.model.item.MediaCreationContent;
import com.arassec.artivact.backend.service.util.ProgressMonitor;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class ExchangeService {

    private final AccountService accountService;

    private final ConfigurationService configurationService;

    private final ItemService itemService;

    private final ProjectRootProvider projectRootProvider;

    private final ObjectMapper exportObjectMapper;

    @Getter
    private ProgressMonitor progressMonitor;

    public ExchangeService(AccountService accountService,
                           ConfigurationService configurationService,
                           ItemService itemService,
                           ProjectRootProvider projectRootProvider,
                           @Qualifier("exportObjectMapper") ObjectMapper exportObjectMapper) {
        this.accountService = accountService;
        this.configurationService = configurationService;
        this.itemService = itemService;
        this.projectRootProvider = projectRootProvider;
        this.exportObjectMapper = exportObjectMapper;
    }

    public StreamingResponseBody createItemExportFile(String itemId) {
        Item item = itemService.loadUnrestricted(itemId);
        item.setMediaCreationContent(null);

        List<String> mediaFiles = itemService.getFilesForDownload(itemId);

        return out -> {
            final ZipOutputStream zipOutputStream = new ZipOutputStream(out);

            ZipEntry zipEntry = new ZipEntry("artivact.item.json");
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(exportObjectMapper.writeValueAsBytes(item));

            for (String mediaFile : mediaFiles) {
                File file = new File(mediaFile);
                zipEntry = new ZipEntry(file.getName());

                try (var inputStream = new FileInputStream(file)) {
                    zipOutputStream.putNextEntry(zipEntry);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = inputStream.read(bytes)) >= 0) {
                        zipOutputStream.write(bytes, 0, length);
                    }
                } catch (IOException e) {
                    log.error("Exception while reading and streaming data!", e);
                }

            }

            zipOutputStream.close();
        };
    }

    public void importItem(MultipartFile file, String apiToken) {
        if (StringUtils.hasText(apiToken)) {
            Optional<Account> accountOptional = accountService.loadByApiToken(apiToken);
            if (accountOptional.isEmpty()) {
                return;
            }
            Account account = accountOptional.get();
            if (!account.getUser() && !account.getAdmin()) {
                return;
            }
        }

        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("import.zip");
        File importFile = projectRootProvider.getProjectRoot().resolve("temp").resolve(originalFilename).toAbsolutePath().toFile();

        try {
            file.transferTo(importFile);
        } catch (IOException e) {
            throw new ArtivactException("Could not save uploaded ZIP file!", e);
        }

        try (ZipFile zipFile = new ZipFile(importFile)) {
            ZipEntry itemJsonZipEntry = zipFile.getEntry("artivact.item.json");

            Item item = exportObjectMapper.readValue(new String(zipFile.getInputStream(itemJsonZipEntry).readAllBytes()), Item.class);
            item.setMediaCreationContent(new MediaCreationContent());

            item.getMediaContent().getImages().forEach(image -> {
                ZipEntry imageZipEntry = zipFile.getEntry(image);
                try {
                    itemService.saveImage(item.getId(), image, zipFile.getInputStream(imageZipEntry), true);
                } catch (IOException e) {
                    throw new ArtivactException("Could not read image from imported ZIP file!", e);
                }
            });

            item.getMediaContent().getModels().forEach(model -> {
                ZipEntry modelZipEntry = zipFile.getEntry(model);
                try {
                    itemService.saveModel(item.getId(), model, zipFile.getInputStream(modelZipEntry), true);
                } catch (IOException e) {
                    throw new ArtivactException("Could not read model from imported ZIP file!", e);
                }
            });

            itemService.save(item);

        } catch (IOException e) {
            throw new ArtivactException("Could not deserialize tags configuration!", e);
        }
    }

    public synchronized void syncItemUp(String itemId) {
        File exportFile = projectRootProvider.getProjectRoot()
                .resolve("temp")
                .resolve(itemId + ".artivact.item.zip")
                .toAbsolutePath()
                .toFile();

        try (FileOutputStream fileOutputStream = new FileOutputStream(exportFile)) {
            StreamingResponseBody streamingResponseBody = createItemExportFile(itemId);
            streamingResponseBody.writeTo(fileOutputStream);
        } catch (IOException e) {
            progressMonitor.updateProgress("Could not create export file to upload item to remote server!", e);
            log.error("Could not create export file to upload item to remote server!", e);
            return;
        }

        ExchangeConfiguration exchangeConfiguration = configurationService.loadExchangeConfiguration();

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(exchangeConfiguration.getRemoteServer()
                    + "/api/exchange/sync/item/import/"
                    + exchangeConfiguration.getApiToken());

            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.STRICT)
                    .addPart("file", new FileBody(exportFile))
                    .build();
            httpPost.setEntity(entity);

            httpclient.execute(httpPost, response -> {
                if (response.getCode() != 200) {
                    progressMonitor.updateProgress("Could not upload item file to remote server!",
                            new ArtivactException("HTTP result code: " + response.getCode()));
                    log.error("Could not upload item file to remote server: HTTP result code " + response.getCode());
                }
                return response;
            });

        } catch (Exception e) {
            progressMonitor.updateProgress("Could not upload item file to remote server!", e);
            log.error("Could not upload item file to remote server!", e);
        }
    }

}
