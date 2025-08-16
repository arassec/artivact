package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.CreateItemModelUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ModelCreatorPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralAdapterInitParams;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final LoadPeripheralConfigurationUseCase loadAdapterConfigurationUseCase;

    /**
     * List of all available adapters.
     */
    private final List<Peripheral> adapters;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void createModel(String itemId) {
        runBackgroundOperationUseCase.execute("createModel", "start", progressMonitor -> {
            Item item = loadItemUseCase.loadTranslated(itemId);

            List<CreationImageSet> modelInputImageSets = item.getMediaCreationContent().getImageSets().stream()
                    .filter(CreationImageSet::isModelInput)
                    .toList();

            Optional<CreationModelSet> modelSetOptional = createModel(itemId, modelInputImageSets, progressMonitor);

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
     * @param creationImageSets The image-sets to use as input for model creation.
     * @param progressMonitor   The progress monitor which will be updated during model creation.
     */
    private Optional<CreationModelSet> createModel(String itemId, List<CreationImageSet> creationImageSets, ProgressMonitor progressMonitor) {
        PeripheralConfiguration adapterConfiguration = loadAdapterConfigurationUseCase.loadPeripheralConfiguration();

        ModelCreatorPeripheral modelCreatorAdapter = adapters.stream()
                .filter(ModelCreatorPeripheral.class::isInstance)
                .map(ModelCreatorPeripheral.class::cast)
                .filter(adapter -> adapter.supports(adapterConfiguration.getModelCreatorPeripheralImplementation()))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected model-creator adapter!"));

        modelCreatorAdapter.initialize(progressMonitor, PeripheralAdapterInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
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

        if (!Files.exists(sourceDir)) {
            log.info("No source directory for model creation at: {}. Model was probably not created...", sourceDir);
            return Optional.empty();
        }

        Path modelsDir = useProjectDirsUseCase.getModelsDir(itemId);

        fileRepository.createDirIfRequired(modelsDir);

        int nextAssetNumber = fileRepository.getNextAssetNumber(modelsDir);

        var targetDir = useProjectDirsUseCase.getModelsDir(itemId)
                .resolve(fileRepository.getAssetName(nextAssetNumber, null));

        fileRepository.createDirIfRequired(targetDir);

        try (Stream<Path> stream = fileRepository.list(sourceDir).stream()) {
            if (stream.findAny().isEmpty()) {
                fileRepository.delete(targetDir);
                throw new ArtivactException("No model files found in export directory: " + sourceDir);
            }

            try (Stream<Path> sourceStream = Files.walk(sourceDir)) {
                sourceStream.forEach(source -> {
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
            }

        } catch (IOException e) {
            fileRepository.delete(targetDir);
            throw new ArtivactException("Could not copy asset directory!", e);
        }

        // Remove the project root from the path: /PATH/TO/REMOVE/items/012/abc/012abc/models/001
        String directory = targetDir.subpath(targetDir.getNameCount() - 6, targetDir.getNameCount())
                .toString().replace("\\", "/");

        return Optional.of(CreationModelSet.builder()
                .directory(directory)
                .comment(result.comment())
                .build());
    }

}
