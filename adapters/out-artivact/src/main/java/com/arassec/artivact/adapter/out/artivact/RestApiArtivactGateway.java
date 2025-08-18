package com.arassec.artivact.adapter.out.artivact;

import com.arassec.artivact.application.port.out.gateway.ArtivactGateway;
import com.arassec.artivact.domain.exception.ArtivactException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Implements the {@link ArtivactGateway} via HTTP/REST.
 */
@Component
public class RestApiArtivactGateway implements ArtivactGateway {

    /**
     * URI part of the import item API.
     */
    private static final String IMPORT_ITEM_API = "api/item/import/";

    /**
     * {@inheritDoc}
     */
    @Override
    public void importItem(String remoteServer, String apiToken, Path exportFile) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(remoteServer + IMPORT_ITEM_API + apiToken);

            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.STRICT)
                    .addPart("file", new FileBody(exportFile.toFile()))
                    .build();
            httpPost.setEntity(entity);

            httpclient.execute(httpPost, response -> {
                if (response.getCode() != 200) {
                    throw new ArtivactException("Could not upload item file to remote server: HTTP result code "
                            + response.getCode() + ", File '" + exportFile + "'");
                }
                return response;
            });
        } catch (IOException e) {
            throw new ArtivactException("Could not upload item!", e);
        }
    }

}
