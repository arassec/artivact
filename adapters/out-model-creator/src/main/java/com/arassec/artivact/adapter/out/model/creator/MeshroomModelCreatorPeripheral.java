package com.arassec.artivact.adapter.out.model.creator;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Adapter implementation for the open source tool "Meshroom".
 */
@Component
@Getter
@RequiredArgsConstructor
public class MeshroomModelCreatorPeripheral extends BaseModelCreatorPeripheral {

    /**
     * The directory containing pipeline files.
     */
    private static final String MESHROOM_DIR = "utils/Meshroom";

    /**
     * The cache directory of Meshroom.
     */
    private static final String MESHROOM_CACHE_DIR = "MeshroomCache";

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
        return PeripheralImplementation.MESHROOM_MODEL_CREATOR_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images) {

        Path projectRoot = initParams.getProjectRoot();
        Path tempDir = initParams.getWorkDir();

        Path resultDir = tempDir.resolve(EXPORT_DIR).toAbsolutePath();
        Path cacheDir = projectRoot.resolve(MESHROOM_DIR).resolve(MESHROOM_CACHE_DIR);

        fileRepository.emptyDir(tempDir);
        fileRepository.emptyDir(resultDir);
        fileRepository.emptyDir(cacheDir);

        copyImages(images, tempDir, progressMonitor);

        fileRepository.openDirInOs(tempDir);

        progressMonitor.updateLabelKey("createModelStart");

        osGateway.execute(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation()),
                List.of(
                        projectRoot.resolve(MESHROOM_DIR).resolve("artivact-meshroom-workflow.mg").toAbsolutePath().toString()
                ));

        return new ModelCreationResult(resultDir, "meshroom");
    }

}
