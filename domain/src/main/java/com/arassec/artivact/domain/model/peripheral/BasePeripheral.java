package com.arassec.artivact.domain.model.peripheral;

import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for adapter implementations.
 */
public abstract class BasePeripheral implements Peripheral {

    /**
     * Saves if the peripheral is currently in use.
     */
    protected final AtomicBoolean inUse = new AtomicBoolean(false);

    /**
     * The progress monitor to give feedback about the adapter status to the user.
     */
    protected ProgressMonitor progressMonitor;

    /**
     * Object with optional parameters for specific adapter implementations.
     */
    protected PeripheralInitParams initParams;

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
    public synchronized void initialize(ProgressMonitor progressMonitor, PeripheralInitParams initParams) {
        this.progressMonitor = progressMonitor;
        this.initParams = initParams;
        this.inUse.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void teardown() {
        this.initParams = null;
        this.inUse.set(false);
    }

}
