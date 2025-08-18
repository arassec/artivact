package com.arassec.artivact.adapter.out.model.editor.peripheral;

import com.arassec.artivact.application.port.out.peripheral.ModelEditorPeripheral;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.peripheral.BasePeripheralAdapter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback peripheral if no model editor is available or should be used.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class FallbackModelEditorPeripheral extends BasePeripheralAdapter implements ModelEditorPeripheral {

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.FALLBACK_MODEL_EDITOR_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(CreationModelSet creationModelSet) {
        log.info("Fallback model editor called for model: {}", creationModelSet.getDirectory());
    }

}
