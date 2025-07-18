package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.ManageItemImagesUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.item.Asset;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageItemImagesService implements ManageItemImagesUseCase {

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    private final UseProjectDirsUseCase useProjectDirsUseCase;

    private final LoadItemUseCase loadItemUseCase;

    private final SaveItemUseCase saveItemUseCase;

    private final FileRepository fileRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadImage(String itemId, String filename, ImageSize targetSize) {
        FileSystemResource fileSystemResource = fileRepository.loadImage(useProjectDirsUseCase.getItemsDir(), itemId, filename, targetSize, DirectoryDefinitions.IMAGES_DIR);
        return fileRepository.readBytes(fileSystemResource.getFile().toPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveImage(String itemId, String filename, InputStream data, boolean keepAssetNumber) {
        fileRepository.saveFile(useProjectDirsUseCase.getProjectRoot(), itemId, filename, data, DirectoryDefinitions.IMAGES_DIR, null, keepAssetNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addImage(String itemId, MultipartFile file) {
        Item item = loadItemUseCase.loadTranslatedRestricted(itemId);

        if (item == null) {
            throw new ArtivactException("No item found with ID: " + itemId);
        }

        try {
            item.getMediaContent().getImages().add(
                    fileRepository.saveFile(useProjectDirsUseCase.getProjectRoot(),
                            itemId,
                            file.getOriginalFilename(),
                            file.getInputStream(),
                            DirectoryDefinitions.IMAGES_DIR,
                            null,
                            false)
            );
        } catch (IOException e) {
            throw new ArtivactException("Could not add image!", e);
        }

        saveItemUseCase.save(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void createImageSetFromDanglingImages(String itemId) {
        runBackgroundOperationUseCase.execute("createImageSet", "start", progressMonitor -> {
            Item item = loadItemUseCase.loadTranslated(itemId);

            List<String> newImages = getDanglingImages(item);

            if (!newImages.isEmpty()) {
                item.getMediaCreationContent().getImageSets().add(CreationImageSet.builder()
                        .backgroundRemoved(null)
                        .modelInput(true)
                        .files(newImages)
                        .build());

                saveItemUseCase.save(item);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openImagesDir(String itemId) {
        fileRepository.openDirInOs(useProjectDirsUseCase.getImagesDir(itemId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferImageToMedia(String itemId, Asset image) {
        Path sourcePath = useProjectDirsUseCase.getImagesDir(itemId).resolve(image.getFileName());
        Path targetPath = getTransferTargetPath(itemId, image);
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            Item item = loadItemUseCase.loadTranslatedRestricted(itemId);
            item.getMediaContent().getImages().add(targetPath.getFileName().toString());
            saveItemUseCase.save(item);
        } catch (IOException e) {
            throw new ArtivactException("Could not copy image!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteImageSet(String itemId, int imageSetIndex) {
        Item item = loadItemUseCase.loadTranslatedRestricted(itemId);
        item.getMediaCreationContent().getImageSets().remove(imageSetIndex);
        saveItemUseCase.save(item);
    }

    /**
     * Returns a list of the item's images that are not referenced by the item itself, but only exist in the
     * filesystem.
     *
     * @param item The item.
     * @return List of unreferenced images.
     */
    private List<String> getDanglingImages(Item item) {
        List<String> imagesInItem = new LinkedList<>(item.getMediaContent().getImages());
        item.getMediaCreationContent().getImageSets().forEach(creationImageSet -> imagesInItem.addAll(creationImageSet.getFiles()));

        List<String> allImagesInFolder = fileRepository.listNamesWithoutScaledImages(
                fileRepository.getDirFromId(useProjectDirsUseCase.getItemsDir(), item.getId()).resolve(DirectoryDefinitions.IMAGES_DIR)
        );

        allImagesInFolder.removeAll(imagesInItem);

        return allImagesInFolder;
    }

    /**
     * Returns the target path to transfer an image from media-creation to media.
     *
     * @param itemId The item's ID.
     * @param image  The image to transfer.
     * @return The target path for the transferred image.
     */
    private Path getTransferTargetPath(String itemId, Asset image) {
        Path imagesDir = useProjectDirsUseCase.getImagesDir(itemId);
        int nextAssetNumber = fileRepository.getNextAssetNumber(imagesDir);
        String extension = fileRepository.getExtension(image.getFileName()).orElseThrow();
        return imagesDir.resolve(fileRepository.getAssetName(nextAssetNumber, extension));
    }

}
