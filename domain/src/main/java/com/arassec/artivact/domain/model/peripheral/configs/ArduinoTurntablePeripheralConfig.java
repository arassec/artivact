package com.arassec.artivact.domain.model.peripheral.configs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuration for an Arduino turntable peripheral.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ArduinoTurntablePeripheralConfig extends PeripheralConfig {

    /**
     * The delay in milliseconds between operations.
     */
    private long delayInMilliseconds;

}
