package com.arassec.artivact.application.port.out.adapter;

import com.arassec.artivact.domain.model.adapter.PeripheralAdapter;
import com.arassec.artivact.domain.model.item.CreationModelSet;

/**
 * Adapter definition for model editors.
 */
public interface ModelEditorAdapter extends PeripheralAdapter {

    /**
     * Opens the 3D model in the adapter's underlying tool.
     *
     * @param creationModelSet The model to open.
     */
    void open(CreationModelSet creationModelSet);

}
