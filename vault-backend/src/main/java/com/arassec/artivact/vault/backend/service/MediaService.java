package com.arassec.artivact.vault.backend.service;

import com.arassec.artivact.vault.backend.core.ArtivactVaultException;
import com.arassec.artivact.vault.backend.service.model.Artivact;
import com.arassec.artivact.vault.backend.service.model.ImageSize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    public static final String IMAGES = "images";

    public static final String SCALED_IMAGES = "scaled-images";

    public static final String MODELS = "models";

    private final ArtivactService artivactService;

    @Value("${artivact.vault.data.dir}")
    private String artivactsDir;

    public byte[] getArtivactImage(String artivactId, String fileName, ImageSize targetSize) {
        try {
            Path originalImagePath = Path.of(artivactsDir)
                    .resolve(artivactId.substring(0, 3))
                    .resolve(artivactId.substring(3, 6))
                    .resolve(artivactId)
                    .resolve(IMAGES)
                    .resolve(fileName);

            Path scaledImageDirPath = Path.of(artivactsDir)
                    .resolve(artivactId.substring(0, 3))
                    .resolve(artivactId.substring(3, 6))
                    .resolve(artivactId)
                    .resolve(SCALED_IMAGES);

            Path scaledImagePath = scaledImageDirPath
                    .resolve(targetSize.name() + "-" + fileName);

            if (!Files.exists(scaledImageDirPath)) {
                Files.createDirectories(scaledImageDirPath);
            }

            String[] fileNameParts = fileName.split("\\.");
            String fileEnding = fileNameParts[fileNameParts.length - 1];

            BufferedImage bufferedImage = null;

            if (!ImageSize.ORIGINAL.equals(targetSize) && !Files.exists(scaledImagePath)) {
                bufferedImage = ImageIO.read(originalImagePath.toFile());
                bufferedImage = Scalr.resize(bufferedImage, targetSize.getWidth());

                try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
                    ImageIO.write(bufferedImage, fileEnding, byteArrayOutputStream);
                    Files.write(scaledImagePath, byteArrayOutputStream.toByteArray());
                }
            }

            if (ImageSize.ORIGINAL.equals(targetSize)) {
                bufferedImage = ImageIO.read(originalImagePath.toFile());
            } else if (bufferedImage == null) {
                bufferedImage = ImageIO.read(scaledImagePath.toFile());
            }

            try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
                ImageIO.write(bufferedImage, fileEnding, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }

        } catch (IOException e) {
            throw new ArtivactVaultException("Could not read artivact image file!", e);
        }
    }

    public FileSystemResource getArtivactModel(String artivactId, String fileName) {
        return new FileSystemResource(Path.of(artivactsDir)
                .resolve(artivactId.substring(0, 3))
                .resolve(artivactId.substring(3, 6))
                .resolve(artivactId)
                .resolve(MODELS)
                .resolve(fileName));
    }

    public List<String> getMediaFilesForDownload(String artivactId) {
        List<String> result = new LinkedList<>();

        Artivact artivact = artivactService.loadArtivact(artivactId, List.of());// roles are not required!

        result.addAll(artivact.getMediaContent().getImages().stream()
                .map(image -> Path.of(artivactsDir)
                        .resolve(artivactId.substring(0, 3))
                        .resolve(artivactId.substring(3, 6))
                        .resolve(artivactId)
                        .resolve(IMAGES)
                        .resolve(image)
                        .toAbsolutePath())
                .map(Path::toString)
                .toList());

        result.addAll(artivact.getMediaContent().getModels().stream()
                .map(model -> Path.of(artivactsDir)
                        .resolve(artivactId.substring(0, 3))
                        .resolve(artivactId.substring(3, 6))
                        .resolve(artivactId)
                        .resolve(MODELS)
                        .resolve(model)
                        .toAbsolutePath())
                .map(Path::toString)
                .toList());

        return result;
    }

}
