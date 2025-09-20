package com.arassec.artivact.adapter.out.camera.peripheral;

import com.arasse.jptp.main.ImageCaptureDevice;
import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.BasePeripheralAdapter;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.jptp.core.datatype.complex.DataObject;
import com.arassec.jptp.core.datatype.complex.DeviceInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Default camera peripheral implementation. Uses the "Picture Transfer Protocol (PTP)" for camera control and image
 * capturing.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class DefaultCameraPeripheral extends BasePeripheralAdapter implements CameraPeripheral {

    /**
     * Image capture device.
     */
    private final ImageCaptureDevice imageCaptureDevice;

    /**
     * The file repository.
     */
    private final FileRepository fileRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.DEFAULT_CAMERA_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ProgressMonitor progressMonitor, PeripheralInitParams initParams) {
        super.initialize(progressMonitor, initParams);
        if (imageCaptureDevice.initialize(Duration.ofSeconds(15), Duration.ofSeconds(15))) {
            DeviceInfo deviceInfo = imageCaptureDevice.getDeviceInfo().orElseThrow();
            log.debug("Using image capture device: {} - {}", deviceInfo.manufacturer(), deviceInfo.model());
        } else {
            throw new ArtivactException("Failed to initialize image capture device");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean captureImage(Path targetFile) {
        try {
            TimeUnit.MILLISECONDS.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Interrupted during camera sleep period.", e);
        }
        Optional<DataObject> dataObject = imageCaptureDevice.captureImage();
        if (dataObject.isPresent()) {
            fileRepository.saveFile(targetFile, dataObject.get().data());
        } else {
            throw new ArtivactException("Could not capture image!");
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void teardown() {
        super.teardown();
        imageCaptureDevice.teardown();
    }

}
