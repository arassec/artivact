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

    private String modelCreatorPeripheralConfigId;

}
