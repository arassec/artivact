package com.arassec.artivact.domain.model.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parameters for model creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateModelParams {

    /**
     * The ID of the model creator peripheral configuration.
     */
    private String modelCreatorPeripheralConfigId;

}
