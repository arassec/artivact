package com.arassec.artivact.adapter.out.turntable;

import com.arassec.artivact.application.port.out.peripheral.TurntablePeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.adapter.BasePeripheralAdapter;
import com.arassec.artivact.domain.model.configuration.AdapterImplementation;
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
public class FallbackTurntableAdapter extends BasePeripheralAdapter implements TurntablePeripheral {

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
    public void rotate(int numPhotos, int turntableDelay) {
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
