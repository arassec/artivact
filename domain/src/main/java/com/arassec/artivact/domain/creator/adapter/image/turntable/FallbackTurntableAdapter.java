package com.arassec.artivact.domain.creator.adapter.image.turntable;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.configuration.AdapterImplementation;
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
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.FALLBACK_TURNTABLE_ADAPTER;
    }

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

}
