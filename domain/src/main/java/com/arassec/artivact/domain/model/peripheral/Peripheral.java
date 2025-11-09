package com.arassec.artivact.domain.model.peripheral;

import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;

import java.util.List;

/**
 * Defines access to a peripheral device or software.
 */
public interface Peripheral {

    /**
     * Returns whether the peripheral supports the given implementation or not.
     *
     * @param peripheralImplementation The implementation to check.
     * @return {@code true}, if the peripheral supports the desired implementation, {@code false} otherwise.
     */
    boolean supports(PeripheralImplementation peripheralImplementation);

    /**
     * Returns the implementation supported by the peripheral.
     *
     * @return The supported implementation.
     */
    PeripheralImplementation getSupportedImplementation();

    /**
     * Initializes the peripheral prior to processing.
     * <p>
     * This method has to be called before using any of the peripheral's functionality!
     *
     * @param progressMonitor The progress monitor which will be updated by the peripheral during processing.
     * @param initParams      General initialization parameters for the peripheral.
     */
    void initialize(ProgressMonitor progressMonitor, PeripheralInitParams initParams);

    /**
     * Tears the peripheral down after processing.
     * <p>
     * This method has to be called after the peripheral has been used!
     */
    void teardown();

    /**
     * Returns the status of the peripheral.
     *
     * @param peripheralConfig Configuration of the peripheral.
     * @return The {@link PeripheralStatus}.
     */
    PeripheralStatus getStatus(PeripheralConfig peripheralConfig);

    /**
     * Scans for peripherals and returns found configurations.
     *
     * @return List of available peripherals.
     */
    List<PeripheralConfig> scanPeripherals();

}
