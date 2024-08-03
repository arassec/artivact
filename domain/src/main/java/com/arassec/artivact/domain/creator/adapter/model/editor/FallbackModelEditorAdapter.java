package com.arassec.artivact.domain.creator.adapter.model.editor;

import com.arassec.artivact.core.model.configuration.AdapterImplementation;
import com.arassec.artivact.core.model.item.CreationModelSet;
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
