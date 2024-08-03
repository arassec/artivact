package com.arassec.artivact.domain.creator.adapter.image.turntable;

import com.arassec.artivact.domain.creator.adapter.AdapterInitParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Initialization parameters for turntable adapters.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TurntableInitParams extends AdapterInitParams {

    /**
     * The delay to use during the communication with the turntable.
     */
    private int turntableDelay;

}
