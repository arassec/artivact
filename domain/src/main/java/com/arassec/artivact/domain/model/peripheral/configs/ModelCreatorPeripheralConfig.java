package com.arassec.artivact.domain.model.peripheral.configs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuration for a model creator peripheral.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ModelCreatorPeripheralConfig extends ExternalProgramPeripheralConfig {

    /**
     * Indicates whether to open the input directory in the operating system.
     */
    private boolean openInputDirInOs;

    /**
     * The directory for storing results.
     */
    private String resultDir;

}
