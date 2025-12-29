package com.arassec.artivact.domain.model.peripheral.configs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuration for an external program peripheral.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExternalProgramPeripheralConfig extends PeripheralConfig {

    /**
     * The command to execute.
     */
    private String command;

    /**
     * The arguments for the command.
     */
    private String arguments;

}

