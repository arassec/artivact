package com.arassec.artivact.vault.backend.service;

import com.arassec.artivact.common.exception.ArtivactException;
import com.arassec.artivact.common.model.Artivact;
import com.arassec.artivact.common.util.FileUtil;
import com.arassec.artivact.vault.backend.core.ArtivactVaultException;
import com.arassec.artivact.vault.backend.service.model.ImageSize;
import com.arassec.artivact.vault.backend.service.model.VaultArtivact;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class MediaService {

    public static final String IMAGES = "images";

    public static final String SCALED_IMAGES = "scaled-images";

    public static final String MODELS = "models";

    private final ArtivactService artivactService;

    private final FileUtil fileUtil;

    private final Path dataDir;

    public MediaService(ArtivactService artivactService, FileUtil fileUtil) {
        this.artivactService = artivactService;
        this.fileUtil = fileUtil;
        this.dataDir = artivactService.getProjectRoot().resolve(Artivact.DATA_DIR);
    }

    public byte[] getArtivactImage(String artivactId, String fileName, ImageSize targetSize) {

        try {
            Path originalImagePath = dataDir
                    .resolve(artivactId.substring(0, 3))
                    .resolve(artivactId.substring(3, 6))
                    .resolve(artivactId)
                    .resolve(IMAGES)
                    .resolve(fileName);

            Path scaledImageDirPath = dataDir
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
        return new FileSystemResource(dataDir
                .resolve(artivactId.substring(0, 3))
                .resolve(artivactId.substring(3, 6))
                .resolve(artivactId)
                .resolve(MODELS)
                .resolve(fileName));
    }

    public List<String> getMediaFilesForDownload(String artivactId) {
        List<String> result = new LinkedList<>();

        VaultArtivact vaultArtivact = artivactService.loadArtivact(artivactId, List.of());// roles are not required!

        result.addAll(vaultArtivact.getMediaContent().getImages().stream()
                .map(image -> dataDir
                        .resolve(artivactId.substring(0, 3))
                        .resolve(artivactId.substring(3, 6))
                        .resolve(artivactId)
                        .resolve(IMAGES)
                        .resolve(image)
                        .toAbsolutePath())
                .map(Path::toString)
                .toList());

        result.addAll(vaultArtivact.getMediaContent().getModels().stream()
                .map(model -> dataDir
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

    @Transactional
    public void addImageToArtivact(String artivactId, MultipartFile file, List<String> roles) {
        VaultArtivact vaultArtivact = artivactService.loadArtivact(artivactId, roles);

        int nextAssetNumber = vaultArtivact.getNextAssetNumber(vaultArtivact.getImagesDir(true));
        String fileExtension = fileUtil.getExtension(file.getOriginalFilename()).orElseThrow();
        Path imagePath = vaultArtivact.getImagePath(true, nextAssetNumber, fileExtension);

        try {
            Files.copy(file.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ArtivactException("Could not save image!", e);
        }

        vaultArtivact.getMediaContent().getImages().add(vaultArtivact.getAssetName(nextAssetNumber, fileExtension));

        artivactService.saveArtivact(vaultArtivact);
    }

    @Transactional
    public void addModelToArtivact(String artivactId, MultipartFile file, List<String> roles) {
        VaultArtivact vaultArtivact = artivactService.loadArtivact(artivactId, roles);

        int nextAssetNumber = vaultArtivact.getNextAssetNumber(vaultArtivact.getModelsDir(true));
        String fileExtension = fileUtil.getExtension(file.getOriginalFilename()).orElseThrow();

        if (!"glb".equals(fileExtension)) {
            throw new ArtivactException("Unsupported model format. Models must be in GLB format!");
        }

        String assetName = vaultArtivact.getAssetName(nextAssetNumber, fileExtension);

        Path modelPath = vaultArtivact.getModelsDir(true).resolve(assetName);

        try {
            Files.copy(file.getInputStream(), modelPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ArtivactException("Could not save model!", e);
        }

        vaultArtivact.getMediaContent().getModels().add(assetName);

        artivactService.saveArtivact(vaultArtivact);
    }

}
