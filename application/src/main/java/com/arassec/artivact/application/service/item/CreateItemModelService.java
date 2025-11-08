package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.CreateItemModelUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ModelCreatorPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.media.CreateModelParams;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateItemModelService implements CreateItemModelUseCase {

    @Getter
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    private final LoadItemUseCase loadItemUseCase;

    private final SaveItemUseCase saveItemUseCase;

    private final FileRepository fileRepository;

    private final LoadPeripheralsConfigurationUseCase loadAdapterConfigurationUseCase;

    /**
     * List of all available peripherals.
     */
    private final List<Peripheral> peripherals;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void createModel(String itemId, CreateModelParams createModelParams) {
        runBackgroundOperationUseCase.execute("createModel", "start", progressMonitor -> {
            Item item = loadItemUseCase.loadTranslated(itemId);

            List<CreationImageSet> modelInputImageSets = item.getMediaCreationContent().getImageSets().stream()
                    .filter(CreationImageSet::isModelInput)
                    .toList();

            Optional<CreationModelSet> modelSetOptional = createModel(itemId, createModelParams, modelInputImageSets, progressMonitor);

            if (modelSetOptional.isPresent()) {
                item.getMediaCreationContent().getModelSets().add(modelSetOptional.get());
                saveItemUseCase.save(item);
            }
        });
    }

    /**
     * Creates a 3D model using the currently configured model-creator adapter.
     *
     * @param itemId            The ID of the item to create a model for.
     * @param createModelParams Parameters for model creation.
     * @param creationImageSets The image-sets to use as input for model creation.
     * @param progressMonitor   The progress monitor which will be updated during model creation.
     */
    public Optional<CreationModelSet> createModel(String itemId, CreateModelParams createModelParams, List<CreationImageSet> creationImageSets, ProgressMonitor progressMonitor) {
        PeripheralsConfiguration peripheralsConfiguration = loadAdapterConfigurationUseCase.loadPeripheralConfiguration();

        PeripheralConfig modelCreatorConfig = peripheralsConfiguration.getModelCreatorPeripheralConfigs().stream()
                .filter(config -> config.getId().equals(createModelParams.getModelCreatorPeripheralConfigId()))
                .findFirst()
                .orElseThrow();

        ModelCreatorPeripheral modelCreatorAdapter = peripherals.stream()
                .filter(ModelCreatorPeripheral.class::isInstance)
                .map(ModelCreatorPeripheral.class::cast)
                .filter(adapter -> adapter.supports(modelCreatorConfig.getPeripheralImplementation()))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected model-creator adapter!"));

        modelCreatorAdapter.initialize(progressMonitor, PeripheralInitParams.builder()
                .config(modelCreatorConfig)
                .workDir(useProjectDirsUseCase.getTempDir())
                .projectRoot(useProjectDirsUseCase.getProjectRoot())
                .build());

        Path imagesDir = useProjectDirsUseCase.getImagesDir(itemId);

        List<Path> images = new LinkedList<>();
        creationImageSets.forEach(creationImageSet -> images.addAll(creationImageSet.getFiles().stream()
                .map(imagesDir::resolve)
                .toList()));

        ModelCreationResult result = modelCreatorAdapter.createModel(images);
        modelCreatorAdapter.teardown();

        Path sourceDir = result.resultDir();

        if (!fileRepository.exists(sourceDir)) {
            log.info("No source directory for model creation at: {}. Model was probably not created...", sourceDir);
            return Optional.empty();
        }

        Path modelsDir = useProjectDirsUseCase.getModelsDir(itemId);

        fileRepository.createDirIfRequired(modelsDir);

        int nextAssetNumber = fileRepository.getNextAssetNumber(modelsDir);

        var targetDir = useProjectDirsUseCase.getModelsDir(itemId)
                .resolve(fileRepository.getAssetName(nextAssetNumber, null));

        fileRepository.createDirIfRequired(targetDir);

        List<Path> sourceFiles = fileRepository.list(sourceDir);

        if (sourceFiles.stream().findAny().isEmpty()) {
            fileRepository.delete(targetDir);
            log.debug("No model files found in export directory: {}", sourceDir);
            return Optional.empty();
        }

        sourceFiles.forEach(source -> {
            if (fileRepository.isDir(source)) {
                return;
            }
            var destination = Paths.get(targetDir.toString(), source.toString().substring(sourceDir.toString().length()));
            try {
                fileRepository.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (ArtivactException e) {
                fileRepository.delete(targetDir);
                throw new ArtivactException("Could not copy model files!", e);
            }
        });

        // Remove the project root from the path: /PATH/TO/REMOVE/items/012/abc/012abc/models/001
        String directory = targetDir.subpath(targetDir.getNameCount() - 6, targetDir.getNameCount())
                .toString().replace("\\", "/");

        return Optional.of(CreationModelSet.builder()
                .directory(directory)
                .comment(result.comment())
                .build());
    }

}
