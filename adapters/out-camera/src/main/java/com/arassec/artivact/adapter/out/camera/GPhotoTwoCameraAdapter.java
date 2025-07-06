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
 * Camera adapter that uses the Linux command line tool "gphoto2".
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class GPhotoTwoCameraAdapter extends BaseCameraAdapter {

    /**
     * The file repository.
     */
    private final FileRepository fileRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.GPHOTO_TWO_CAMERA_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean captureImage(String filename) {
        var cmdLine = new CommandLine(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation()));
        cmdLine.addArgument("--filename");
        cmdLine.addArgument(initParams.getWorkDir().resolve(filename + ".jpg").toAbsolutePath().toString());
        cmdLine.addArgument("--capture-image-and-download");
        execute(cmdLine);
        return true;
    }

}
