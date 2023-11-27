package com.arassec.artivact.backend.service.creator.adapter.image.turntable;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Fallback turntable adapter in case none could/should be used.
 */
@Slf4j
@Component
@Getter
public class FallbackTurntableAdapter extends BaseTurntableAdapter {

    /**
     * The implementation supported by this adapter.
     */
    private final AdapterImplementation supportedImplementation = AdapterImplementation.FALLBACK_TURNTABLE_ADAPTER;

    /**
     * {@inheritDoc}
     */
    @Override
    public void rotate(int numPhotos) {
        int turntableDelay = initParams.getTurntableDelay();
        log.info("Fallback turntable 'rotate' called with 'numPhotos': {} and 'turntableDelay': {}", numPhotos, turntableDelay);
        if (turntableDelay > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(turntableDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ArtivactException("Interrupted during fallback turntable delay!", e);
            }
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
