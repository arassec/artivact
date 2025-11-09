package com.arassec.artivact.adapter.out.camera.peripheral;

import com.arasse.jptp.main.ImageCaptureDevice;
import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.BasePeripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import com.arassec.artivact.domain.model.peripheral.configs.PtpCameraPeripheralConfig;
import com.arassec.jptp.core.datatype.complex.DataObject;
import com.arassec.jptp.core.datatype.complex.DeviceInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Camera peripheral implementation. Uses the "Picture Transfer Protocol (PTP)" for camera control and image
 * capturing.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class PtpCameraPeripheral extends BasePeripheral implements CameraPeripheral {

    /**
     * Image capture device.
     */
    private final ImageCaptureDevice imageCaptureDevice;

    /**
     * The file repository.
     */
    private final FileRepository fileRepository;

    /**
     * Delay before capturing an image.
     */
    private long delayInMilliseconds;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.PTP_CAMERA_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void initialize(ProgressMonitor progressMonitor, PeripheralInitParams initParams) {
        super.initialize(progressMonitor, initParams);
        delayInMilliseconds = ((PtpCameraPeripheralConfig) initParams.getConfig()).getDelayInMilliseconds();
        if (delayInMilliseconds <= 0) {
            delayInMilliseconds = 250;
        }
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
            TimeUnit.MILLISECONDS.sleep(delayInMilliseconds);
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
    public synchronized void teardown() {
        super.teardown();
        imageCaptureDevice.teardown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralStatus getStatus(PeripheralConfig peripheralConfig) {
        if (inUse.get()) {
            return PeripheralStatus.AVAILABLE;
        }
        if (imageCaptureDevice.initialize(Duration.ofSeconds(15), Duration.ofSeconds(15))) {
            imageCaptureDevice.teardown();
            return PeripheralStatus.AVAILABLE;
        }
        return PeripheralStatus.DISCONNECTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PeripheralConfig> scanPeripherals() {
        if (inUse.get()) {
            return List.of();
        }

        if (imageCaptureDevice.initialize(Duration.ofSeconds(15), Duration.ofSeconds(15))) {
            imageCaptureDevice.teardown();

            PtpCameraPeripheralConfig peripheralConfig = new PtpCameraPeripheralConfig();
            peripheralConfig.setPeripheralImplementation(PeripheralImplementation.PTP_CAMERA_PERIPHERAL);
            peripheralConfig.setFavourite(true);
            peripheralConfig.setLabel("PTP Camera");
            peripheralConfig.setDelayInMilliseconds(250);

            return List.of(peripheralConfig);
        }

        return List.of();
    }

}
