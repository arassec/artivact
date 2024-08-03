package com.arassec.artivact.domain.creator.adapter.image.camera;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.configuration.AdapterImplementation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.FALLBACK_CAMERA_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void captureImage(String filename) {
        log.info("Fallback camera adapter called with 'targetDir' {} and 'filename' {}", initParams.getTargetDir(), filename);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactException("Interrupted during sleep while simulating photo capture!", e);
        }
    }

}
