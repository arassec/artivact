package com.arassec.artivact.backend.service.creator;

import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.creator.adapter.Adapter;
import com.arassec.artivact.backend.service.creator.adapter.model.creator.ModelCreationResult;
import com.arassec.artivact.backend.service.creator.adapter.model.creator.ModelCreatorAdapter;
import com.arassec.artivact.backend.service.creator.adapter.model.creator.ModelCreatorInitParams;
import com.arassec.artivact.backend.service.creator.adapter.model.editor.ModelEditorAdapter;
import com.arassec.artivact.backend.service.creator.adapter.model.editor.ModelEditorInitParams;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.ProjectDir;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.model.item.asset.ImageSet;
import com.arassec.artivact.backend.service.model.item.asset.Model;
import com.arassec.artivact.backend.service.util.FileUtil;
import com.arassec.artivact.backend.service.util.ProgressMonitor;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private final ProjectRootProvider projectRootProvider;

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
    @Getter
    private final List<Adapter<?, ?>> adapters;

    /**
     * Creates a 3D model using the currently configured model-creator adapter.
     *
     * @param itemId          The ID of the item to create a model for.
     * @param imageSets       The image-sets to use as input for model creation.
     * @param pipeline        The pipeline to use.
     * @param progressMonitor The progress monitor which will be updated during model creation.
     */
    public Optional<Model> createModel(String itemId, List<ImageSet> imageSets, String pipeline, ProgressMonitor progressMonitor) {
        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();
        ModelCreatorAdapter modelCreatorAdapter = getModelCreatorAdapter(adapterConfiguration);

        modelCreatorAdapter.initialize(progressMonitor, ModelCreatorInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
                .tempDir(projectRootProvider.getProjectRoot().resolve(ProjectDir.TEMP_DIR))
                .build());
        ModelCreationResult result = modelCreatorAdapter.createModel(imageSets, pipeline);
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
            if (stream.findAny().isEmpty()) {
                fileUtil.deleteDir(targetDirWithProjectRoot);
                throw new ArtivactException("Source for model creation not present: " + sourceDir);
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

        Model model = Model.builder()
                .number(nextAssetNumber)
                .path(formatPath(targetDir))
                .comment(result.comment())
                .build();

        return Optional.of(model);
    }

    /**
     * Returns the default pipeline provided by the current model-creator.
     *
     * @return The name of the model-creator's default pipeline.
     */
    public String getDefaultPipeline() {
        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();
        return getModelCreatorAdapter(adapterConfiguration).getDefaultPipeline().orElse("");
    }

    /**
     * Returns all available pipelines of the current model-creator.
     *
     * @return List of pipeline names.
     */
    public List<String> getPipelines() {
        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();
        return getModelCreatorAdapter(adapterConfiguration).getPipelines();
    }

    /**
     * Cancels model creation.
     */
    public void cancelModelCreation() {
        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();
        getModelCreatorAdapter(adapterConfiguration).cancelModelCreation();
    }

    /**
     * Returns whether model creation cancelling is supported or not.
     *
     * @return {@code true} if model creation can be cancelled, {@code false} otherwise.
     */
    public boolean cancelModelCreationSupported() {
        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();
        return getModelCreatorAdapter(adapterConfiguration).supportsCancellation();
    }

    /**
     * Opens the selected model in the currently configured model-editor.
     *
     * @param progressMonitor The progress monitor to show status updates to the user.
     * @param model           The model to open.
     */
    public void openModel(ProgressMonitor progressMonitor, Model model) {
        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();
        ModelEditorAdapter modelEditorAdapter = getModelEditorAdapter(adapterConfiguration);

        modelEditorAdapter.initialize(progressMonitor, ModelEditorInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
                .build());
        modelEditorAdapter.open(model);
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
            return getAssetDir(itemId, projectRootProvider.getProjectRoot(), ProjectDir.MODELS_DIR);
        }
        return getAssetDir(itemId, null, ProjectDir.MODELS_DIR);
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
     * Adds a 3D model to the item with the given ID.
     *
     * @param itemId    The ID of the item to get the directory for.
     * @param modelFile Path to the model file to add.
     */
    public Model addModel(String itemId, Path modelFile) {
        Path modelsDir = getModelsDir(itemId, true);

        fileUtil.createDirIfRequired(modelsDir);

        int nextAssetNumber = getNextAssetNumber(modelsDir);

        var targetDir = getModelDir(itemId, false, nextAssetNumber);
        var targetDirWithProjectRoot = getModelDir(itemId, true, nextAssetNumber);

        fileUtil.createDirIfRequired(targetDirWithProjectRoot);

        var destination = Paths.get(targetDirWithProjectRoot.toString(), modelFile.getFileName().toString());
        try {
            Files.copy(modelFile, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            fileUtil.deleteDir(targetDirWithProjectRoot);
            throw new ArtivactException("Could not copy model files!", e);
        }

        return Model.builder()
                .number(nextAssetNumber)
                .path(formatPath(targetDir))
                .comment("import")
                .build();
    }

    /**
     * Deletes the model with the given index from the list of models of the currently active item.
     *
     * @param model The model to delete from the filesystem.
     */
    public void deleteModel(Model model) {
        if (model != null) {
            fileUtil.deleteDir(projectRootProvider.getProjectRoot().resolve(model.getPath()));
        }
    }

}
