package com.arassec.artivact.domain.creator.adapter.image.background;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.configuration.AdapterConfiguration;
import com.arassec.artivact.core.model.configuration.AdapterImplementation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Background removal adapter that uses rembg, running in a docker container or remote, via HTTP.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class RemBgRemoteBackgroundRemovalAdapter extends BaseRemBgBackgroundRemovalAdapter {

    /**
     * Error message template.
     */
    private static final String HTTP_ERROR_MSG_TEMPLATE = "Could not use RemBg remotely: %s - %s";

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER;
    }

    /**
     * Removes the background by calling the rembg API via HTTP.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @param inputFile            The original image with background.
     * @param targetDir            The directory to put the resulting image in.
     * @return Path to the newly created image file without background.
     */
    @Override
    protected Path removeBackgroundFromImage(AdapterConfiguration adapterConfiguration, Path inputFile, Path targetDir) {
        // Java sadly doesn't support multipart file upload natively, so here we use the Apache HTTP client.
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            File payload = inputFile.toFile();

            HttpPost httpPost = new HttpPost(adapterConfiguration.getConfigValue(getSupportedImplementation()));

            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.STRICT)
                    .addPart("file", new FileBody(payload))
                    .build();
            httpPost.setEntity(entity);

            String[] fileNameParts = inputFile.getFileName().toString().split("\\.");
            String outputFilename = String.join(".", Arrays.copyOf(fileNameParts, fileNameParts.length - 1)) + ".png";

            Path outputFile = targetDir.resolve(outputFilename);

            httpclient.execute(httpPost, response -> {
                FileUtils.copyInputStreamToFile(response.getEntity().getContent(), outputFile.toFile());
                EntityUtils.consume(response.getEntity());
                return response;
            });

            return outputFile;
        } catch (Exception e) {
            log.error("Could not remotely remove background from image!", e);
            throw new ArtivactException("Could not remotely remove background from image!", e);
        }
    }

}
