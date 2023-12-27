package com.arassec.artivact.backend.service.creator.adapter.model.editor;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.model.item.CreationModelSet;
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
public class FallbackModelEditorAdapter extends BaseModelEditorAdapter {

    /**
     * The implementation supported by this adapter.
     */
    private final AdapterImplementation supportedImplementation = AdapterImplementation.FALLBACK_MODEL_EDITOR_ADAPTER;

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(CreationModelSet creationModelSet) {
        log.info("Fallback model editor called for model: {}", creationModelSet.getDirectory());
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
