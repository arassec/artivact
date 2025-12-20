package com.arassec.artivact.adapter.out.image.background.removal.peripheral;


import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.arassec.artivact.adapter.out.image.background.removal.peripheral.onnx.OnnxBackgroundRemover;
import com.arassec.artivact.adapter.out.image.background.removal.peripheral.onnx.OnnxBackgroundRemoverParams;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ImageManipulatorPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.BasePeripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.OnnxBackgroundRemovalPeripheralConfig;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Peripheral for background removal using ONNX. Uses neural networks for salient object detection.
 * <p>
 * Configuration options tested so far are:
 * <p>
 * silueta.onnx#input.1#320#320#5
 * BiRefNet-HRSOD_DHU-epoch_115.onnx#input_image#1024#1024#1
 * u2netp.onnx#input.1#320#320#5
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OnnxImageBackgroundRemovalPeripheral extends BasePeripheral implements ImageManipulatorPeripheral {

    private final FileRepository fileRepository;

    /**
     * An executor service to remove backgrounds multithreaded.
     */
    private ExecutorService executorService;

    /**
     * The result list containing paths to the processed image files without a background.
     */
    protected final Collection<Path> result = Collections.synchronizedCollection(new LinkedList<>());

    /**
     * The ONNX runtime environment.
     */
    private OrtEnvironment environment;

    /**
     * The ONNX runtime session.
     */
    private OrtSession session;

    /**
     * Use case to get project directories.
     */
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Parameters for the background remover thread.
     */
    private OnnxBackgroundRemoverParams onnxParams;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void initialize(ProgressMonitor progressMonitor, PeripheralInitParams initParams) {
        super.initialize(progressMonitor, initParams);

        onnxParams = new OnnxBackgroundRemoverParams(((OnnxBackgroundRemovalPeripheralConfig) initParams.getConfig()), initParams.getWorkDir());

        if (environment == null) {
            setupEnvironment(onnxParams.getOnnxModelFileName()
                    .replace("{projectDir}", initParams.getProjectRoot().toAbsolutePath().toString()));
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
    public synchronized void teardown() {
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

        try {
            session.close();
            session = null;
            environment.close();
            environment = null;
        } catch (OrtException e) {
            throw new ArtivactException("Could not teardown ONNX session!", e);
        }

        // Cleanup AFTER the threads have finished!
        super.teardown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralStatus getStatus(PeripheralConfig peripheralConfig) {
        if (inUse.get()) {
            return PeripheralStatus.AVAILABLE;
        }
        OnnxBackgroundRemovalPeripheralConfig config = (OnnxBackgroundRemovalPeripheralConfig) peripheralConfig;

        Path onnxFile = Path.of(config.getOnnxModelFile()
                .replace("{projectDir}", useProjectDirsUseCase.getProjectRoot().toAbsolutePath().toString()));

        if (!Files.exists(onnxFile)) {
            return PeripheralStatus.FILE_DOESNT_EXIST;
        }

        return PeripheralStatus.AVAILABLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PeripheralConfig> scanPeripherals() {
        Path onnxFile = useProjectDirsUseCase.getProjectRoot().resolve("utils/onnx/silueta.onnx");
        if (fileRepository.exists(onnxFile)) {
            OnnxBackgroundRemovalPeripheralConfig peripheralConfig = new OnnxBackgroundRemovalPeripheralConfig();
            peripheralConfig.setPeripheralImplementation(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL);
            peripheralConfig.setLabel("Silueta");
            peripheralConfig.setFavourite(true);
            peripheralConfig.setOnnxModelFile("{projectDir}/utils/onnx/silueta.onnx");
            peripheralConfig.setInputParameterName("input.1");
            peripheralConfig.setImageWidth(320);
            peripheralConfig.setImageHeight(320);
            peripheralConfig.setNumThreads(5);
            return List.of(peripheralConfig);
        }
        return List.of();
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
    private void setupEnvironment(String modelPath) {
        try {
            environment = OrtEnvironment.getEnvironment();

            OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
            sessionOptions.setInterOpNumThreads(4);
            sessionOptions.addCPU(true);

            session = environment.createSession(modelPath, sessionOptions);
        } catch (OrtException e) {
            throw new ArtivactException("Could not create ONNX environment!", e);
        }
    }

}
