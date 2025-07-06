package com.arassec.artivact.adapter.out.model.editor;

import com.arassec.artivact.application.port.out.adapter.ModelEditorAdapter;
import com.arassec.artivact.domain.model.adapter.BasePeripheralAdapter;
import com.arassec.artivact.domain.model.configuration.AdapterImplementation;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback adapter if not model editor is available or should be used.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class FallbackModelEditorAdapter extends BasePeripheralAdapter implements ModelEditorAdapter {

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.FALLBACK_MODEL_EDITOR_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(CreationModelSet creationModelSet) {
        log.info("Fallback model editor called for model: {}", creationModelSet.getDirectory());
    }

}
