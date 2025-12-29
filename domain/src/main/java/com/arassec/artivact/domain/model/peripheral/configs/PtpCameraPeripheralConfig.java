package com.arassec.artivact.domain.model.peripheral.configs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuration for a PTP camera peripheral.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PtpCameraPeripheralConfig extends PeripheralConfig {

    /**
     * The delay in milliseconds between operations.
     */
    private long delayInMilliseconds;

}
