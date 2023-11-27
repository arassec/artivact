package com.arassec.artivact.backend.service.creator.adapter.model.editor;

import com.arassec.artivact.backend.service.creator.adapter.Adapter;
import com.arassec.artivact.backend.service.model.item.asset.Model;

/**
 * Adapter definition for model editors.
 */
public interface ModelEditorAdapter extends Adapter<ModelEditorInitParams, Void> {

    /**
     * Opens the 3D model in the adapter's underlying tool.
     *
     * @param model The model to open.
     */
    void open(Model model);

}
