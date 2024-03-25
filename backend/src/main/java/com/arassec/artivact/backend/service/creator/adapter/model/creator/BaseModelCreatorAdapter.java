package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.BaseAdapter;

/**
 * Base class for model-creator adapter implementations.
 */
public abstract class BaseModelCreatorAdapter
        extends BaseAdapter<ModelCreatorInitParams, Void> implements ModelCreatorAdapter {

    /**
     * The export subdirectory where model creators should export their results in.
     */
    protected static final String EXPORT_DIR = "export/";

}
