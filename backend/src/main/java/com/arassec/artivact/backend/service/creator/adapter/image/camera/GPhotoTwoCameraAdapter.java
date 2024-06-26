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
 * Camera adapter that uses the Linux command line tool "gphoto2".
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class GPhotoTwoCameraAdapter extends BaseCameraAdapter {

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
        return AdapterImplementation.GPHOTO_TWO_CAMERA_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void captureImage(String filename) {
        var cmdLine = new CommandLine(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation()));
        cmdLine.addArgument("--filename");
        cmdLine.addArgument(initParams.getTargetDir().resolve(filename + ".jpg").toAbsolutePath().toString());
        cmdLine.addArgument("--capture-image-and-download");
        cmdUtil.execute(cmdLine);
    }

}
