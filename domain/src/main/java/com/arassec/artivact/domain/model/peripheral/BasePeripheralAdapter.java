package com.arassec.artivact.domain.model.peripheral;

import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;

/**
 * Base class for adapter implementations.
 */
public abstract class BasePeripheralAdapter implements Peripheral {

    /**
     * The progress monitor to give feedback about the adapter status to the user.
     */
    protected ProgressMonitor progressMonitor;

    /**
     * Object with optional parameters for specific adapter implementations.
     */
    protected PeripheralAdapterInitParams initParams;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(PeripheralImplementation peripheralImplementation) {
        return getSupportedImplementation().equals(peripheralImplementation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ProgressMonitor progressMonitor, PeripheralAdapterInitParams initParams) {
        this.progressMonitor = progressMonitor;
        this.initParams = initParams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void teardown() {
        this.initParams = null;
    }

}
