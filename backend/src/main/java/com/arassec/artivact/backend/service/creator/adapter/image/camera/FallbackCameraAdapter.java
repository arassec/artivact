package com.arassec.artivact.backend.service.creator.adapter.image.camera;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.util.FileUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Fallback camera adapter in case no real camera can/should be used.
 * <p>
 * Uses fallback images instead of real photos.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class FallbackCameraAdapter extends BaseCameraAdapter {

    /**
     * The implementation supported by this adapter.
     */
    private final AdapterImplementation supportedImplementation = AdapterImplementation.FALLBACK_CAMERA_ADAPTER;

    /**
     * The file util.
     */
    private final FileUtil fileUtil;

    /**
     * {@inheritDoc}
     */
    @Override
    public void captureImage(String filename) {
        Path targetFile = initParams.getTargetDir().resolve(filename + ".jpg");
        log.info("Fallback camera adapter called with 'targetDir' {} and 'filename' {}", initParams.getTargetDir(), filename);
        fileUtil.copyClasspathResource(Path.of("project-setup/utils/fallback-image.jpg"),
                initParams.getTargetDir().getParent());
        try {
            Files.move(initParams.getTargetDir().getParent().resolve("fallback-image.jpg"), targetFile, StandardCopyOption.REPLACE_EXISTING);
            Thread.sleep(1000);
        } catch (IOException e) {
            throw new ArtivactException("Could not create fallback image!", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactException("Could not create fallback image!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration(AdapterConfiguration adapterConfiguration) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean healthy(AdapterConfiguration adapterConfiguration) {
        return true;
    }

}
