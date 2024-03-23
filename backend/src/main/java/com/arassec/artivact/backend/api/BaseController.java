package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.OperationProgress;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.item.Item;
import com.arassec.artivact.backend.service.misc.ProgressMonitor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Base for REST-Controllers with utility methods.
 */
@Slf4j
public abstract class BaseController {

    /**
     * Cache header value.
     */
    protected static final String NO_CACHE = "no-cache";

    /**
     * Attachment header prefix.
     */
    protected static final String ATTACHMENT_PREFIX = "attachment; filename=";

    /**
     * Expires header value.
     */
    protected static final String EXPIRES_IMMEDIATELY = "0";

    /**
     * Mime type for ZIP files.
     */
    protected static final String TYPE_ZIP = "application/zip";

    /**
     * Creates the URL to an item's main image.
     *
     * @param item The item to create the main image URL for.
     * @return The (relative) URL as string.
     */
    protected String createMainImageUrl(Item item) {
        if (!item.getMediaContent().getImages().isEmpty()) {
            return createImageUrl(item.getId(), item.getMediaContent().getImages().getFirst());
        }
        return null;
    }

    /**
     * Create the URL to an image with the given filename.
     *
     * @param itemId   The item's ID.
     * @param filename The file's name.
     * @return The (relative) URL as string.
     */
    protected String createImageUrl(String itemId, String filename) {
        return createUrl(itemId, filename, "image");
    }

    /**
     * Creates the image for a given model-set, based on the files available in the set.
     *
     * @param modelSetDir The directory of the model-set.
     * @return The (relative) URL as string.
     */
    protected String createModelSetImageUrl(Path modelSetDir) {
        try (Stream<Path> files = Files.list(modelSetDir)) {
            List<String> availableExtensions = files.map(file -> file.getFileName().toString())
                    .filter(f -> f.contains("."))
                    .map(fileName -> fileName.substring(fileName.lastIndexOf(".") + 1))
                    .toList();

            if (availableExtensions.contains("glb") || availableExtensions.contains("gltf")) {
                return "gltf-logo.png";
            } else if (availableExtensions.contains("blend")) {
                return "blender-logo.png";
            } else if (availableExtensions.contains("obj")) {
                return "obj-logo.png";
            }

            return "unknown-file-logo.png";
        } catch (IOException e) {
            return "unknown-file-logo.png";
        }
    }

    /**
     * Creates the URL to an item's model.
     *
     * @param itemId   The item's ID.
     * @param filename The model's filename
     * @return The (relative) URL as string.
     */
    protected String createModelUrl(String itemId, String filename) {
        return createUrl(itemId, filename, "model");
    }

    /**
     * Creates a URL to a given file of an item.
     *
     * @param itemId   The item's ID.
     * @param fileName The name of the file.
     * @param fileType The type of file, i.e. 'image' or 'model'.
     * @return The (relative) URL as string.
     */
    protected String createUrl(String itemId, String fileName, String fileType) {
        return "/api/item/" + itemId + "/" + fileType + "/" + fileName;
    }

    /**
     * Converts a {@link ProgressMonitor} into a {@link OperationProgress}.
     *
     * @param progressMonitor The progress monitor to convert.
     * @return An operation progress containing the monitor's data.
     */
    protected ResponseEntity<OperationProgress> convert(ProgressMonitor progressMonitor) {
        if (progressMonitor != null) {
            OperationProgress operationProgress = new OperationProgress();
            operationProgress.setKey(progressMonitor.getLabelKey());
            operationProgress.setCurrentAmount(progressMonitor.getCurrentAmount());
            operationProgress.setTargetAmount(progressMonitor.getTargetAmount());
            if (progressMonitor.getException() != null) {
                operationProgress.setError(ExceptionUtils.getStackTrace(progressMonitor.getException()));
            }
            return ResponseEntity.ok(operationProgress);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * ZIPs the provided media files into a file written to the given output stream.
     *
     * @param zipOutputStream The target stream.
     * @param mediaFiles      The list of files to pack.
     */
    protected void zipMediaFiles(ZipOutputStream zipOutputStream, List<String> mediaFiles) {
        ZipEntry zipEntry;
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
                throw new ArtivactException("Exception while reading and streaming data!", e);
            }
        }

        try {
            zipOutputStream.close();
        } catch (IOException e) {
            throw new ArtivactException("Could not create ZIP file!", e);
        }
    }

}
