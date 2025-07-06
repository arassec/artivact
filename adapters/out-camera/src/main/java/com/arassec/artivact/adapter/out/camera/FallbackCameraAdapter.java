package com.arassec.artivact.adapter.out.camera;

import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.AdapterImplementation;
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
    public boolean captureImage(String filename) {
        log.info("Fallback camera adapter called with 'targetDir' {} and 'filename' {}", initParams.getWorkDir(), filename);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactException("Interrupted during sleep while simulating photo capture!", e);
        }
        return true;
    }

}
