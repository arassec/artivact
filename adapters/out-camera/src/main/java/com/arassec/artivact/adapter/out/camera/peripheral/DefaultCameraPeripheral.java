package com.arassec.artivact.adapter.out.camera.peripheral;

import com.arasse.jptp.main.ImageCaptureDevice;
import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.peripheral.BasePeripheralAdapter;
import com.arassec.artivact.domain.model.peripheral.PeripheralAdapterInitParams;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.jptp.core.datatype.complex.DataObject;
import com.arassec.jptp.core.datatype.complex.DeviceInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

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
    public void initialize(ProgressMonitor progressMonitor, PeripheralAdapterInitParams initParams) {
        super.initialize(progressMonitor, initParams);

        // Might happen when teardown() is not called because of errors!
        if (imageCaptureDevice.isInitialized()) {
            imageCaptureDevice.teardown();
        }

        if (imageCaptureDevice.initialize()) {
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
    public void teardown() {
        super.teardown();
        imageCaptureDevice.teardown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean captureImage(Path targetFile) {

        Optional<DataObject> dataObject = imageCaptureDevice.captureImage();
        if (dataObject.isPresent()) {
            fileRepository.saveFile(targetFile, dataObject.get().data());
        } else {
            throw new ArtivactException("Could not capture image!");
        }

        return true;
    }

}
