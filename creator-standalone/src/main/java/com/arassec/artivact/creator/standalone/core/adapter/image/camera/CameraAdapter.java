package com.arassec.artivact.creator.standalone.core.adapter.image.camera;

import java.nio.file.Path;

public interface CameraAdapter {

    void captureImage(Path targetDir, int index);

}
