package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.ManageItemModelsUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.item.Asset;
import com.arassec.artivact.domain.model.item.CreationModelSet;
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
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageItemModelsService implements ManageItemModelsUseCase {

    private final FileRepository fileRepository;

    private final LoadItemUseCase loadItemUseCase;

    private final SaveItemUseCase saveItemUseCase;

    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadModel(String itemId, String filename) {
        FileSystemResource fileSystemResource = new FileSystemResource(
                useProjectDirsUseCase.getItemsDir()
                        .resolve(itemId.substring(0, 3))
                        .resolve(itemId.substring(3, 6))
                        .resolve(itemId)
                        .resolve(DirectoryDefinitions.MODELS_DIR)
                        .resolve(filename));
        return fileRepository.readBytes(fileSystemResource.getFile().toPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addModel(String itemId, MultipartFile file) {
        Item item = loadItemUseCase.loadTranslatedRestricted(itemId);

        if (item == null) {
            throw new ArtivactException("No item found with ID: " + itemId);
        }

        try {
            item.getMediaContent().getModels().add(
                    fileRepository.saveFile(useProjectDirsUseCase.getProjectRoot(),
                            itemId, file.getOriginalFilename(), file.getInputStream(), DirectoryDefinitions.MODELS_DIR, "glb", false)
            );
        } catch (IOException e) {
            throw new ArtivactException("Could not add model!", e);
        }

        saveItemUseCase.save(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveModel(String itemId, String filename, InputStream data, boolean keepAssetNumber) {
        fileRepository.saveFile(useProjectDirsUseCase.getProjectRoot(), itemId, filename, data, DirectoryDefinitions.MODELS_DIR, null, keepAssetNumber);
    }

    /**
     * Opens the item's models directory with a local file browser.
     *
     * @param itemId The item's ID.
     */
    @Override
    public void openModelsDir(String itemId) {
        fileRepository.openDirInOs(useProjectDirsUseCase.getModelsDir(itemId));
    }

    /**
     * Opens a specific model directory of an item with a local file browser.
     *
     * @param itemId The item's ID.
     */
    @Override
    public void openModelDir(String itemId, int modelSetIndex) {
        Item item = loadItemUseCase.loadTranslatedRestricted(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);
        fileRepository.openDirInOs(useProjectDirsUseCase.getProjectRoot().resolve(creationModelSet.getDirectory()));
    }

    /**
     * Returns the {@link Asset}s of an item's model-set.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The index of the model-set to load the assets from.
     * @return List of assets in the model-set.
     */
    @Override
    public List<Asset> getModelSetFiles(String itemId, int modelSetIndex) {
        Item item = loadItemUseCase.loadTranslatedRestricted(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);
        try (Stream<Path> files = Files.list(useProjectDirsUseCase.getProjectRoot().resolve(creationModelSet.getDirectory()))) {
            return files
                    .map(file -> Asset.builder()
                            .fileName(file.getFileName().toString())
                            .url(createModelLogoUrl(file.getFileName().toString()))
                            .transferable(file.getFileName().toString().endsWith("glb") || file.getFileName().toString().endsWith("gltf"))
                            .build())
                    .toList();
        } catch (IOException e) {
            log.error("Could not read files of model-set!", e);
            return List.of();
        }
    }

    /**
     * Transfers a model from an item's media-creation section to its media.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex Index to the model-set containing the model file.
     * @param model         The model to transfer.
     */
    @Override
    public void transferModelToMedia(String itemId, int modelSetIndex, Asset model) {
        Item item = loadItemUseCase.loadTranslatedRestricted(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);

        Path sourcePath = useProjectDirsUseCase.getProjectRoot().resolve(creationModelSet.getDirectory()).resolve(model.getFileName());
        Path targetPath = getTransferTargetPath(itemId, model);
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            item.getMediaContent().getModels().add(targetPath.getFileName().toString());
            saveItemUseCase.save(item);
        } catch (IOException e) {
            throw new ArtivactException("Could not copy model!", e);
        }
    }

    /**
     * Deletes an item's model-set.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The index of the model-set to delete.
     */
    @Override
    public void deleteModelSet(String itemId, int modelSetIndex) {
        Item item = loadItemUseCase.loadTranslatedRestricted(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);
        fileRepository.delete(useProjectDirsUseCase.getProjectRoot().resolve(Path.of(creationModelSet.getDirectory())).toAbsolutePath());
        item.getMediaCreationContent().getModelSets().remove(modelSetIndex);
        saveItemUseCase.save(item);
    }

    /**
     * Toggles the "model-input" property of an image-set.
     *
     * @param itemId        The item's ID.
     * @param imageSetIndex The index to the image-set that should be modified.
     */
    @Override
    public void toggleModelInput(String itemId, int imageSetIndex) {
        Item item = loadItemUseCase.loadTranslatedRestricted(itemId);
        item.getMediaCreationContent().getImageSets().get(imageSetIndex).setModelInput(
                !item.getMediaCreationContent().getImageSets().get(imageSetIndex).isModelInput()
        );
        saveItemUseCase.save(item);
    }

    /**
     * Creates a logo file to display for a given model file.
     *
     * @param filename The filename to determine the logo for.
     * @return The logo filename.
     */
    private String createModelLogoUrl(String filename) {
        String file = filename.toLowerCase();
        if (file.endsWith("glb") || file.endsWith("gltf")) {
            return "gltf-logo.png";
        } else if (file.endsWith("blend") || file.endsWith("blend1")) {
            return "blender-logo.png";
        } else if (file.endsWith("obj") || file.endsWith("mtl")) {
            return "obj-logo.png";
        } else if (file.endsWith("jpg") || file.endsWith("jpeg") || file.endsWith("png")) {
            return "image-file-logo.png";
        }
        return "unknown-file-logo.png";
    }

    /**
     * Returns the target path to transfer a model from media-creation to media.
     *
     * @param itemId The item's ID.
     * @param model  The model to transfer.
     * @return The target path for the transferred model.
     */
    private Path getTransferTargetPath(String itemId, Asset model) {
        Path modelsDir = useProjectDirsUseCase.getModelsDir(itemId);
        int nextAssetNumber = fileRepository.getNextAssetNumber(modelsDir);
        String extension = fileRepository.getExtension(model.getFileName()).orElseThrow();
        return modelsDir.resolve(fileRepository.getAssetName(nextAssetNumber, extension));
    }

}
