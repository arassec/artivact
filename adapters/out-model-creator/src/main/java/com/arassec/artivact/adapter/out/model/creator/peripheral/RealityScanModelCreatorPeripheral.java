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
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter implementation that uses "RealityScan" from Epic for model creation.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class RealityScanModelCreatorPeripheral extends BaseModelCreatorPeripheral {

    /**
     * The directory containing pipeline files.
     */
    private static final String REALITY_CAPTURE_EXPORT_SETTINGS = "utils/RealityScan/realityscan-export-settings.xml";

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
        return PeripheralImplementation.REALITY_SCAN_MODEL_CREATOR_PERIPHERAL;
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

        progressMonitor.updateLabelKey("createModelStart");

        String command = initParams.getConfiguration().getConfigValue(getSupportedImplementation());
        boolean headless = false;
        String faceCount = "200000";
        if (command.contains("#")) {
            String[] commandParts = command.split("#");
            command = commandParts[0];
            if (commandParts.length > 1) {
                headless = "headless".equals(commandParts[1]);
            }
            if (commandParts.length > 2) {
                faceCount = commandParts[2];
            }
        }

        List<String> arguments = new ArrayList<>();
        arguments.add("-addFolder");
        arguments.add(tempDir.toAbsolutePath().toString());
        arguments.add("-save");
        arguments.add(tempDir.toAbsolutePath() + "/MyProject.rcproj");

        if (headless) {
            arguments.add("-headless");
            arguments.add("-align");
            arguments.add("-setReconstructionRegionAuto");
            arguments.add("-calculateNormalModel");
            arguments.add("-simplify");
            arguments.add(faceCount);
            arguments.add("-calculateTexture");
            arguments.add("-exportSelectedModel");
            arguments.add(resultDir.resolve("RealityScanExport.obj").toAbsolutePath().toString());
            arguments.add(initParams.getProjectRoot().resolve(REALITY_CAPTURE_EXPORT_SETTINGS).toAbsolutePath().toString());
            arguments.add("-quit");
        }

        osGateway.execute(command, arguments);

        return new ModelCreationResult(resultDir, "RealityScan");
    }

}
