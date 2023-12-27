package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.model.item.Item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public abstract class BaseFileController {

    protected String createMainImageUrl(Item item) {
        if (!item.getMediaContent().getImages().isEmpty()) {
            return createImageUrl(item.getId(), item.getMediaContent().getImages().get(0));
        }
        return null;
    }

    protected String createImageUrl(String itemId, String fileName) {
        return createUrl(itemId, fileName, "image");
    }

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

    protected String createModelUrl(String itemId, String fileName) {
        return createUrl(itemId, fileName, "model");
    }

    protected String createUrl(String itemId, String fileName, String fileType) {
        return "/api/item/" + itemId + "/" + fileType + "/" + fileName;
    }

}
