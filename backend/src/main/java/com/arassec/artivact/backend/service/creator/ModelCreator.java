package com.arassec.artivact.backend.service.creator;

import com.arassec.artivact.backend.service.creator.adapter.model.creator.FallbackModelCreatorAdapter;
import com.arassec.artivact.backend.service.model.item.Asset;
import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.creator.adapter.Adapter;
import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.creator.adapter.model.creator.ModelCreationResult;
import com.arassec.artivact.backend.service.creator.adapter.model.creator.ModelCreatorAdapter;
import com.arassec.artivact.backend.service.creator.adapter.model.creator.ModelCreatorInitParams;
import com.arassec.artivact.backend.service.creator.adapter.model.editor.ModelEditorAdapter;
import com.arassec.artivact.backend.service.creator.adapter.model.editor.ModelEditorInitParams;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.model.item.CreationImageSet;
import com.arassec.artivact.backend.service.model.item.CreationModelSet;
import com.arassec.artivact.backend.service.util.FileUtil;
import com.arassec.artivact.backend.service.misc.ProgressMonitor;
import com.arassec.artivact.backend.service.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Service for 3D model creation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelCreator extends BaseCreator {

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * Provides the project's root directory.
     */
    @Getter
    private final ProjectDataProvider projectDataProvider;

    /**
     * The file util.
     */
    private final FileUtil fileUtil;

    /**
     * The object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * List of all available adapters.
     */
    private final List<Adapter<?, ?>> adapters;

    /**
     * Creates a 3D model using the currently configured model-creator adapter.
     *
     * @param itemId            The ID of the item to create a model for.
     * @param creationImageSets The image-sets to use as input for model creation.
     * @param progressMonitor   The progress monitor which will be updated during model creation.
     */
    public Optional<CreationModelSet> createModel(String itemId, List<CreationImageSet> creationImageSets, ProgressMonitor progressMonitor) {
        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();
        ModelCreatorAdapter modelCreatorAdapter = getModelCreatorAdapter(adapterConfiguration);

        modelCreatorAdapter.initialize(progressMonitor, ModelCreatorInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
                .tempDir(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.TEMP_DIR))
                .projectRoot(projectDataProvider.getProjectRoot())
                .build());

        Path imagesDir = getImagesDir(itemId, true);

        List<Path> images = new LinkedList<>();
        creationImageSets.forEach(creationImageSet -> images.addAll(creationImageSet.getFiles().stream()
                .map(imagesDir::resolve)
                .toList()));

        ModelCreationResult result = modelCreatorAdapter.createModel(images);
        modelCreatorAdapter.teardown();

        Path sourceDir = result.resultDir();

        if (!Files.exists(sourceDir)) {
            log.info("No source directory for model creation at: {}. Model was probably not created...", sourceDir);
            return Optional.empty();
        }

        Path modelsDir = getModelsDir(itemId, true);

        fileUtil.createDirIfRequired(modelsDir);

        int nextAssetNumber = getNextAssetNumber(modelsDir);

        var targetDir = getModelDir(itemId, false, nextAssetNumber);
        var targetDirWithProjectRoot = getModelDir(itemId, true, nextAssetNumber);

        fileUtil.createDirIfRequired(targetDirWithProjectRoot);

        try (Stream<Path> stream = Files.list(sourceDir)) {
            if (stream.findAny().isEmpty() && !(modelCreatorAdapter instanceof FallbackModelCreatorAdapter)) {
                fileUtil.deleteDir(targetDirWithProjectRoot);
                throw new ArtivactException("No model files found in export directory: " + sourceDir);
            }

            try (Stream<Path> sourceStream = Files.walk(sourceDir)) {
                sourceStream.forEach(source -> {
                    if (Files.isDirectory(source)) {
                        return;
                    }
                    var destination = Paths.get(targetDirWithProjectRoot.toString(), source.toString().substring(sourceDir.toString().length()));
                    try {
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        fileUtil.deleteDir(targetDirWithProjectRoot);
                        throw new ArtivactException("Could not copy model files!", e);
                    }
                });
            }

        } catch (IOException e) {
            fileUtil.deleteDir(targetDirWithProjectRoot);
            throw new ArtivactException("Could not copy asset directory!", e);
        }

        return Optional.of(CreationModelSet.builder()
                .directory(formatPath(targetDir))
                .comment(result.comment())
                .build());
    }

    /**
     * Opens the selected model in the currently configured model-editor.
     *
     * @param progressMonitor The progress monitor to show status updates to the user.
     * @param creationModel   The model to open.
     */
    public void editModel(ProgressMonitor progressMonitor, CreationModelSet creationModel) {
        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();
        ModelEditorAdapter modelEditorAdapter = getModelEditorAdapter(adapterConfiguration);

        modelEditorAdapter.initialize(progressMonitor, ModelEditorInitParams.builder()
                .projectRoot(projectDataProvider.getProjectRoot())
                .adapterConfiguration(adapterConfiguration)
                .build());
        modelEditorAdapter.open(creationModel);
        modelEditorAdapter.teardown();
    }

    /**
     * Returns the models directory of the currently active item.
     *
     * @param itemId             The ID of the item to get the directory for.
     * @param includeProjectRoot If {@code true}, the model directory path will be appended to the project's root path.
     * @return Path to the model directory.
     */
    public Path getModelsDir(String itemId, boolean includeProjectRoot) {
        if (includeProjectRoot) {
            return getAssetDir(itemId, projectDataProvider.getProjectRoot(), ProjectDataProvider.MODELS_DIR);
        }
        return getAssetDir(itemId, null, ProjectDataProvider.MODELS_DIR);
    }

    /**
     * Returns the directory of the 3D model with the given asset number.
     *
     * @param itemId             The ID of the item to get the directory for.
     * @param includeProjectRoot If {@code true}, the model directory path will be appended to the project's root path.
     * @param assetNumber        The asset number of the 3D model.
     * @return The path to the model's directory.
     */
    public Path getModelDir(String itemId, boolean includeProjectRoot, int assetNumber) {
        return getModelsDir(itemId, includeProjectRoot).resolve(getAssetName(assetNumber, null));
    }

    /**
     * Returns the target path to transfer a model from media-creation to media.
     *
     * @param itemId The item's ID.
     * @param model  The model to transfer.
     * @return The target path for the transferred model.
     */
    public Path getTransferTargetPath(String itemId, Asset model) {
        Path modelsDir = getModelsDir(itemId, true);
        int nextAssetNumber = getNextAssetNumber(modelsDir);
        return modelsDir.resolve(getAssetName(nextAssetNumber, FilenameUtils.getExtension(model.getFileName())));
    }

    /**
     * Returns the desired adapter.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @return The configured {@link ModelCreatorAdapter}.
     */
    private ModelCreatorAdapter getModelCreatorAdapter(AdapterConfiguration adapterConfiguration) {
        AdapterImplementation adapterImplementation = adapterConfiguration.getModelCreatorImplementation();
        return adapters.stream()
                .filter(ModelCreatorAdapter.class::isInstance)
                .map(ModelCreatorAdapter.class::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException(NO_ADAPTER_ERROR));
    }

    /**
     * Returns the desired adapter.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @return The configured {@link ModelEditorAdapter}.
     */
    private ModelEditorAdapter getModelEditorAdapter(AdapterConfiguration adapterConfiguration) {
        AdapterImplementation adapterImplementation = adapterConfiguration.getModelEditorImplementation();
        return adapters.stream()
                .filter(ModelEditorAdapter.class::isInstance)
                .map(ModelEditorAdapter.class::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException(NO_ADAPTER_ERROR));
    }

}
