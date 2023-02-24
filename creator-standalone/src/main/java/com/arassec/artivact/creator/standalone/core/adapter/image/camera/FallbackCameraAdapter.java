package com.arassec.artivact.creator.standalone.core.adapter.image.camera;

import com.arassec.artivact.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class FallbackCameraAdapter extends BaseCameraAdapter {

    private final FileUtil fileUtil;

    @Override
    public void captureImage(Path targetDir, int index) {
        log.info("Fallback camera adapter called with 'targetDir' {} and 'index' {}", targetDir, index);
        fileUtil.copyClasspathResource(Path.of("project-setup/Utils/fallback-image.png"),
                targetDir);
    }

}
