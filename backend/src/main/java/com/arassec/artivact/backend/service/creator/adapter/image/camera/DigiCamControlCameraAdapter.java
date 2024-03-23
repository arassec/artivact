package com.arassec.artivact.backend.service.creator.adapter.image.camera;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
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
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.DIGI_CAM_CONTROL_CAMERA_ADAPTER;
    }

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

}
