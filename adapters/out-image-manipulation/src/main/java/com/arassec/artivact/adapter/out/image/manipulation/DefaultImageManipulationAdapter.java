package com.arassec.artivact.adapter.out.image.manipulation;


import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.arassec.artivact.adapter.out.image.manipulation.onnx.OnnxBackgroundRemover;
import com.arassec.artivact.adapter.out.image.manipulation.onnx.OnnxBackgroundRemoverParams;
import com.arassec.artivact.application.port.out.adapter.ImageManipulationAdapter;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.adapter.PeripheralAdapterInitParams;
import com.arassec.artivact.domain.model.adapter.BasePeripheralAdapter;
import com.arassec.artivact.domain.model.configuration.AdapterImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Default adapter for background removal. Uses neural networks for salient object detection.
 * <p>
 * Configuration options tested so far are:
 * <p>
 * silueta.onnx#input.1#320#320#5
 * BiRefNet-HRSOD_DHU-epoch_115.onnx#input_image#1024#1024#1
 * u2netp.onnx#input.1#320#320#5
 */
@Slf4j
@Getter
@Component
public class DefaultImageManipulationAdapter extends BasePeripheralAdapter implements ImageManipulationAdapter {

    /**
     * An executor service to remove backgrounds multithreaded.
     */
    protected ExecutorService executorService;

    /**
     * The result list containing paths to the processed image files without a background.
     */
    protected final Collection<Path> result = Collections.synchronizedCollection(new LinkedList<>());

    /**
     * The ONNX runtime environment.
     */
    protected OrtEnvironment environment;

    /**
     * The ONNX runtime session.
     */
    protected OrtSession session;

    /**
     * Parameters for the background remover thread.
     */
    private OnnxBackgroundRemoverParams onnxParams;

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.DEFAULT_BACKGROUND_REMOVAL_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ProgressMonitor progressMonitor, PeripheralAdapterInitParams initParams) {
        super.initialize(progressMonitor, initParams);

        onnxParams = new OnnxBackgroundRemoverParams(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation()), initParams);

        if (environment == null) {
            setupEnvironment(initParams, onnxParams);
        }

        onnxParams.setEnvironment(environment);
        onnxParams.setSession(session);

        executorService = Executors.newFixedThreadPool(onnxParams.getNumThreads());

        result.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeBackground(Path filePath) {
        // Set the monitor parameter to 'null' to avoid progress updates, which are in this case handled by the calling class!
        removeBackgroundInternal(filePath, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeBackgrounds(List<Path> filePaths) {
        progressMonitor.updateLabelKey("backgroundRemovalInProgress");
        filePaths.forEach(filePath -> removeBackgroundInternal(filePath, progressMonitor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Path> getModifiedImages() {
        return new LinkedList<>(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void teardown() {
        executorService.shutdown();

        boolean shutdown = false;
        while (!shutdown) {
            try {
                shutdown = executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ArtivactException("Interrupted during image background removal!", e);
            }
        }

        // Cleanup AFTER the threads have finished!
        super.teardown();
    }

    /**
     * Called to actually remove the background from the given image.
     *
     * @param filePath The path to the image to remove the background from.
     * @param monitor  The progress monitor to update.
     */
    private void removeBackgroundInternal(Path filePath, ProgressMonitor monitor) {
        executorService.submit(new OnnxBackgroundRemover(onnxParams, result, filePath, monitor));
    }

    /**
     * Creates the ONNX environment.
     */
    private void setupEnvironment(PeripheralAdapterInitParams initParams, OnnxBackgroundRemoverParams onnxParams) {
        try {
            environment = OrtEnvironment.getEnvironment();

            OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
            sessionOptions.setInterOpNumThreads(4);
            sessionOptions.addCPU(true);

            session = environment.createSession(initParams.getProjectRoot()
                    .resolve("utils/onnx/").resolve(onnxParams.getOnnxModelFileName()).toString(), sessionOptions);
        } catch (OrtException e) {
            throw new ArtivactException("Could not create ONNX environment!", e);
        }
    }

}
