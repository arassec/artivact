package com.arassec.artivact.adapter.out.model.creator.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Adapter implementation that uses "Metashape" for model creation.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class MetashapeModelCreatorPeripheral extends BaseModelCreatorPeripheral {

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
        return PeripheralImplementation.METASHAPE_MODEL_CREATOR_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images) {
        Path tempDir = initParams.getWorkDir();

        Path resultDir = tempDir.resolve(EXPORT_DIR).toAbsolutePath();

        fileRepository.emptyDir(tempDir);
        fileRepository.createDirIfRequired(resultDir);

        copyImages(images, tempDir, progressMonitor);

        fileRepository.openDirInOs(tempDir);

        progressMonitor.updateLabelKey("createModelStart");

        osGateway.execute(initParams.getConfiguration().getConfigValue(getSupportedImplementation()), List.of());

        return new ModelCreationResult(resultDir, "metashape");
    }

}
