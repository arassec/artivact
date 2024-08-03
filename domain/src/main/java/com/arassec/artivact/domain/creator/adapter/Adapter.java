package com.arassec.artivact.domain.creator.adapter;

import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.configuration.AdapterImplementation;

import java.util.Optional;

/**
 * An adapter to an external program.
 *
 * @param <I> Type of the mandatory initialization parameters.
 * @param <T> Result type of the teardown method call.
 */
public interface Adapter<I extends AdapterInitParams, T> {

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
     * @param initParams      Object with parameters for specific adapter implementations.
     */
    void initialize(ProgressMonitor progressMonitor, I initParams);

    /**
     * Tears the adapter down after processing.
     * <p>
     * This method has to be called after the adapter has been used!
     *
     * @return Adapter result of the processing, if any.
     */
    Optional<T> teardown();

}
