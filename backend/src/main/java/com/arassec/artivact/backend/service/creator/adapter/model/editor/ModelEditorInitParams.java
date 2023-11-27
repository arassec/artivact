package com.arassec.artivact.backend.service.creator.adapter.model.editor;

import com.arassec.artivact.backend.service.creator.adapter.AdapterInitParams;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Init parameters for model-creator adapters.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ModelEditorInitParams extends AdapterInitParams {

    /**
     * The current adapter configuration.
     */
    private AdapterConfiguration adapterConfiguration;

}
