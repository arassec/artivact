package com.arassec.artivact.domain.model.adapter;

import com.arassec.artivact.domain.model.configuration.AdapterImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;

/**
 * Base class for adapter implementations.
 */
public abstract class BasePeripheralAdapter implements PeripheralAdapter {

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
    public boolean supports(AdapterImplementation adapterImplementation) {
        return getSupportedImplementation().equals(adapterImplementation);
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
