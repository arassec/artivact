package com.arassec.artivact.domain.model.adapter;

import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.configuration.AdapterImplementation;

/**
 * An adapter to an external program or piece of hardware.
 */
public interface PeripheralAdapter {

    /**
     * Returns whether the adapter supports the given implementation or not.
     *
     * @param adapterImplementation The implementation to check.
     * @return {@code true}, if the adapter supports the desired implementation, {@code false} otherwise.
     */
    boolean supports(AdapterImplementation adapterImplementation);

    /**
     * Returns the implementation supported by the adapter.
     *
     * @return The supported implementation.
     */
    AdapterImplementation getSupportedImplementation();

    /**
     * Initializes the adapter prior to processing.
     * <p>
     * This method has to be called prior to using any of the adapter's functionality!
     *
     * @param progressMonitor The progress monitor which will be updated by the adapter during processing.
     * @param initParams      General initialization parameters for the adapter.
     */
    void initialize(ProgressMonitor progressMonitor, PeripheralAdapterInitParams initParams);

    /**
     * Tears the adapter down after processing.
     * <p>
     * This method has to be called after the adapter has been used!
     */
    void teardown();

}
