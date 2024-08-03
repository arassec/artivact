package com.arassec.artivact.domain.creator.adapter.model.editor;

import com.arassec.artivact.core.model.configuration.AdapterConfiguration;
import com.arassec.artivact.domain.creator.adapter.AdapterInitParams;
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
