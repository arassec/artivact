package com.arassec.artivact.adapter.out.model.creator.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.peripheral.ModelCreatorPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.BasePeripheral;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.ExternalProgramPeripheralConfig;
import com.arassec.artivact.domain.model.peripheral.configs.ModelCreatorPeripheralConfig;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for model-creator adapter implementations.
 */
@Component
@Getter
@RequiredArgsConstructor
public class ExternalProgramModelCreatorPeripheral extends BasePeripheral implements ModelCreatorPeripheral {

    /**
     * The file repository.
     */
    private final FileRepository fileRepository;

    /**
     * Gateway to the operating system.
     */
    private final OsGateway osGateway;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralStatus getStatus(PeripheralConfig peripheralConfig) {
        if (inUse.get()) {
            return PeripheralStatus.AVAILABLE;
        }

        if (osGateway.isExecutable(((ExternalProgramPeripheralConfig) peripheralConfig).getCommand())) {
            return PeripheralStatus.AVAILABLE;
        }

        return PeripheralStatus.NOT_EXECUTABLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images) {
        Path tempDir = initParams.getWorkDir();

        fileRepository.emptyDir(tempDir);

        copyImages(images, tempDir, progressMonitor);

        ModelCreatorPeripheralConfig config = ((ModelCreatorPeripheralConfig) initParams.getConfig());
        Path resultDir = Path.of(config.getResultDir().replace("{projectDir}", initParams.getProjectRoot().toAbsolutePath().toString()));

        fileRepository.emptyDir(resultDir);
        fileRepository.createDirIfRequired(resultDir);

        if (config.isOpenInputDirInOs()) {
            fileRepository.openDirInOs(tempDir);
        }

        progressMonitor.updateLabelKey("createModelStart");

        String command = config.getCommand();
        String[] arguments = config.getArguments()
                .replace("{projectDir}", initParams.getProjectRoot().toAbsolutePath().toString())
                .replace("\n", " ")
                .split(" ");


        osGateway.execute(command, Arrays.asList(arguments));

        return new ModelCreationResult(resultDir, config.getLabel());
    }

    /**
     * Copies all images from the provided {@link CreationImageSet} to the provided destination.
     *
     * @param images          The images to copy.
     * @param destination     The destination path to copy the images to.
     * @param progressMonitor A progress monitor which is updated during copying.
     */
    protected void copyImages(List<Path> images, Path destination, ProgressMonitor progressMonitor) {
        progressMonitor.updateLabelKey("copyImages");
        var index = new AtomicInteger(1);
        images.forEach(image -> {
            progressMonitor.updateProgress(index.getAndIncrement(), images.size());
            getFileRepository().copy(image, destination.resolve(image.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        });
    }

}
