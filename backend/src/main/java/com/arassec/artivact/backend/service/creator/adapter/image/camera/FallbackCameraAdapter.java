package com.arassec.artivact.backend.service.creator.adapter.image.camera;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
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
     * The implementation supported by this adapter.
     */
    private final AdapterImplementation supportedImplementation = AdapterImplementation.FALLBACK_CAMERA_ADAPTER;

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
