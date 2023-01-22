package com.arassec.artivact.creator.core.adapter.image.camera;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Slf4j
@Service
@ConditionalOnProperty(value = "adapter.implementation.camera", havingValue = "DigiCamControl")
public class DigiCamControlCameraAdapter extends BaseCameraAdapter {

    @Value("${adapter.implementation.camera.executable}")
    private String executable;

    public void captureImage(Path targetDir, int index) {

        var cmdLine = new CommandLine(executable);
        cmdLine.addArgument("/folder");
        cmdLine.addArgument(targetDir.toAbsolutePath().toString());
        cmdLine.addArgument("/capture");

        executeCommandLine(cmdLine);
    }

}
