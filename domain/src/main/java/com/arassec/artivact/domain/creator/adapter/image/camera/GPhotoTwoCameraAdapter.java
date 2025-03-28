package com.arassec.artivact.domain.creator.adapter.image.camera;

import com.arassec.artivact.core.model.configuration.AdapterImplementation;
import com.arassec.artivact.core.repository.FileRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Component;

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
        cmdLine.addArgument(initParams.getTargetDir().resolve(filename + ".jpg").toAbsolutePath().toString());
        cmdLine.addArgument("--capture-image-and-download");
        execute(cmdLine);
        return true;
    }

}
