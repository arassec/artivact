package com.arassec.artivact.creator.standalone.core.adapter.image.background;

import com.arassec.artivact.creator.standalone.core.model.Artivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.standalone.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.standalone.core.util.FileHelper;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "adapter.implementation.background", havingValue = "RemBgRemote")
public class RemBgRemoteBackgroundRemovalAdapter extends BaseRemBgBackgroundRemovalAdapter {

    private final FileHelper fileHelper;

    private final MessageSource messageSource;

    @Value("${adapter.implementation.background.executable}")
    private String remoteUrl;

    @Override
    public List<Path> removeBackgroundFromImages(Artivact artivact, ArtivactImageSet imageSet, ProgressMonitor progressMonitor) {
        initializeEnvironment(artivact, imageSet, progressMonitor, fileHelper);

        List<Path> result = new LinkedList<>();

        progressMonitor.setProgressPrefix(messageSource.getMessage("background-removal-adapter.rembg.progress.prefix", null,
                Locale.getDefault()));

        ExecutorService executor = Executors.newFixedThreadPool(4);

        try (Stream<Path> filePaths = Files.list(remBgInputDir)) {
            filePaths.forEach(filePath -> executor.submit(() -> {
                log.debug("Removing background from image: {}", filePath.getFileName());
                result.add(removeBackground(filePath, remBgOutputDir));
            }));
        } catch (IOException | ArtivactCreatorException e) {
            throw new ArtivactCreatorException("Could not remove Background of image!", e);
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ArtivactCreatorException("Interrupted during image background removal!", e);
            }
        }

        return result;
    }

    private Path removeBackground(Path inputFile, Path remBgOutputDir) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            File payload = inputFile.toFile();

            HttpPost httpPost = new HttpPost(remoteUrl);

            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.STRICT)
                    .addPart("file", new FileBody(payload))
                    .build();
            httpPost.setEntity(entity);

            CloseableHttpResponse response = httpclient.execute(httpPost);

            if (response.getCode() != 200) {
                throw new ArtivactCreatorException("Could not remove Background from image, HTTP-Status: " + response.getCode());
            }

            String[] fileNameParts = inputFile.getFileName().toString().split("\\.");
            String outputFilename = String.join(".", Arrays.copyOf(fileNameParts, fileNameParts.length - 1)) + ".png";

            Path outputFile = remBgOutputDir.resolve(outputFilename);

            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), outputFile.toFile());

            EntityUtils.consume(response.getEntity());

            return outputFile;

        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not remotely remove background from image!", e);
        }
    }

}
