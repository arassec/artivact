package com.arassec.artivact.domain.creator.adapter.model.editor;

import com.arassec.artivact.core.model.item.CreationModelSet;
import com.arassec.artivact.domain.creator.adapter.Adapter;

/**
 * Adapter definition for model editors.
 */
public interface ModelEditorAdapter extends Adapter<ModelEditorInitParams, Void> {

    /**
     * Opens the 3D model in the adapter's underlying tool.
     *
     * @param creationModelSet The model to open.
     */
    void open(CreationModelSet creationModelSet);

}
