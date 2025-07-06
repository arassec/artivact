package com.arassec.artivact.adapter.out.camera;

import com.arassec.artivact.domain.model.configuration.AdapterImplementation;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.Executor;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
     * The file repository.
     */
    private final FileRepository fileRepository;

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
    public boolean captureImage(String filename) {
        log.debug("Starting image capturing with DigiCamControl.");

        var cmdLine = new CommandLine(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation()));
        cmdLine.addArgument("/folder");
        cmdLine.addArgument(initParams.getWorkDir().toAbsolutePath().toString());
        cmdLine.addArgument("/capture");

        execute(cmdLine);

        log.debug("Finished image capturing with DigiCamControl.");

        return true;
    }

}
