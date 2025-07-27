package com.arassec.artivact.adapter.out.camera;

import com.arasse.jptp.main.ImageCaptureDevice;
import com.arasse.jptp.main.PtpImageCaptureDevice;
import com.arassec.jptp.usb.UsbPtpDeviceDiscovery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring-Configuration for the camera peripheral adapter.
 */
@Configuration
public class CameraAdapterConfiguration {

    /**
     * Picture-Transfer-Protocol image capture device used by the default camera peripheral adapter.
     */
    @Bean
    public ImageCaptureDevice imageCaptureDevice() {
        return new PtpImageCaptureDevice(new UsbPtpDeviceDiscovery());
    }

}
