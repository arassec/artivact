package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.util.CmdUtil;
import com.arassec.artivact.backend.service.util.FileUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Adapter implementation that uses "RealityCapture" from Epic for model creation.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class RealityCaptureModelCreatorAdapter extends BaseModelCreatorAdapter {

    /**
     * The directory containing pipeline files.
     */
    private static final String REALITY_CAPTURE_EXPORT_SETTINGS = "utils/RealityCapture/realitycapture-export-settings.xml";

    /**
     * The commandline util.
     */
    private final CmdUtil cmdUtil;

    /**
     * The file util.
     */
    private final FileUtil fileUtil;

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.REALITY_CAPTURE_MODEL_CREATOR_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images) {
        Path tempDir = initParams.getTempDir();

        Path resultDir = tempDir.resolve(EXPORT_DIR).toAbsolutePath();

        fileUtil.emptyDir(tempDir);

        copyImages(images, tempDir, progressMonitor);

        progressMonitor.updateLabelKey("createModelStart");

        String command = initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation());
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

        var cmdLine = new CommandLine(command);
        cmdLine.addArgument("-addFolder");
        cmdLine.addArgument(tempDir.toAbsolutePath().toString());
        cmdLine.addArgument("-save");
        cmdLine.addArgument(tempDir.toAbsolutePath() + "/MyProject.rcproj");

        if (headless) {
            cmdLine.addArgument("-headless");
            cmdLine.addArgument("-align");
            cmdLine.addArgument("-setReconstructionRegionAuto");
            cmdLine.addArgument("-calculateNormalModel");
            cmdLine.addArgument("-simplify");
            cmdLine.addArgument(faceCount);
            cmdLine.addArgument("-calculateTexture");
            cmdLine.addArgument("-exportSelectedModel");
            cmdLine.addArgument(resultDir.resolve("RealityCaptureExport.obj").toAbsolutePath().toString());
            cmdLine.addArgument(initParams.getProjectRoot().resolve(REALITY_CAPTURE_EXPORT_SETTINGS).toAbsolutePath().toString());
            cmdLine.addArgument("-quit");
        }

        cmdUtil.execute(cmdLine);

        return new ModelCreationResult(resultDir, "RealityCapture");

    }
}
