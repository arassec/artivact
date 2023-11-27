package com.arassec.artivact.backend.service.creator.adapter.image.camera;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.util.CmdUtil;
import com.arassec.artivact.backend.service.util.FileUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Component;

/**
 * Camera adapter for the Windows open source tool "DigiCamControl".
 * <p>
 * This implementation uses the <a href="https://digicamcontrol.com/doc/userguide/cmd">Command Line Utility</a>
 * of the application.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class DigiCamControlCameraAdapter extends BaseCameraAdapter {

    /**
     * The implementation supported by this adapter.
     */
    private final AdapterImplementation supportedImplementation = AdapterImplementation.DIGI_CAM_CONTROL_CAMERA_ADAPTER;

    /**
     * The commandline util.
     */
    private final CmdUtil cmdUtil;

    /**
     * The commandline util.
     */
    private final FileUtil fileUtil;

    /**
     * {@inheritDoc}
     */
    @Override
    public void captureImage(String filename) {
        log.debug("Starting image capturing with DigiCamControl.");

        var cmdLine = new CommandLine(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation()));
        cmdLine.addArgument("/folder");
        cmdLine.addArgument(initParams.getTargetDir().toAbsolutePath().toString());
        cmdLine.addArgument("/capture");

        cmdUtil.execute(cmdLine);

        log.debug("Finished image capturing with DigiCamControl.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration(AdapterConfiguration adapterConfiguration) {
        if (!cmdUtil.execute(new CommandLine(adapterConfiguration.getConfigValue(getSupportedImplementation())))) {
            throw new ArtivactException("Configured file could not be executed!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean healthy(AdapterConfiguration adapterConfiguration) {
        try {
            return fileUtil.isFileExecutable(adapterConfiguration.getConfigValue(getSupportedImplementation()));
        } catch (Exception e) {
            return false;
        }
    }

}
