package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.application.port.out.peripheral.ImageManipulatorPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptureItemImageServiceTest {

    private CaptureItemImageService service;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @Mock
    private LoadItemUseCase loadItemUseCase;

    @Mock
    private SaveItemUseCase saveItemUseCase;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private LoadPeripheralsConfigurationUseCase loadAdapterConfigurationUseCase;

    @Mock
    private CameraPeripheral cameraPeripheral;

    @Mock
    private ImageManipulatorPeripheral imageManipulatorPeripheral;

    @BeforeEach
    void setUp() {
        List<Peripheral> peripherals = new ArrayList<>();
        peripherals.add(cameraPeripheral);
        peripherals.add(imageManipulatorPeripheral);

        service = new CaptureItemImageService(
                useProjectDirsUseCase,
                runBackgroundOperationUseCase,
                loadItemUseCase,
                saveItemUseCase,
                fileRepository,
                loadAdapterConfigurationUseCase,
                peripherals
        );
    }

    @Test
    void captureImageCapturesWithoutBackgroundRemoval() {
        String itemId = "item-123";
        Path imagesDir = Path.of("/project/items/item-123/images");
        Path targetFile = imagesDir.resolve("001.jpg");

        PeripheralConfig cameraConfig = mock(PeripheralConfig.class);
        when(cameraConfig.getId()).thenReturn("camera-config-1");
        when(cameraConfig.getPeripheralImplementation()).thenReturn(PeripheralImplementation.PTP_CAMERA_PERIPHERAL);

        PeripheralsConfiguration peripheralsConfiguration = PeripheralsConfiguration.builder()
                .cameraPeripheralConfigs(List.of(cameraConfig))
                .imageBackgroundRemovalPeripheralConfigs(List.of())
                .build();

        when(loadAdapterConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(peripheralsConfiguration);
        when(useProjectDirsUseCase.getImagesDir(itemId)).thenReturn(imagesDir);
        when(fileRepository.getNextAssetNumber(imagesDir)).thenReturn(1);
        when(fileRepository.getAssetName(1, "jpg")).thenReturn("001.jpg");
        when(cameraPeripheral.supports(PeripheralImplementation.PTP_CAMERA_PERIPHERAL)).thenReturn(true);
        when(cameraPeripheral.captureImage(any(Path.class))).thenReturn(true);

        com.arassec.artivact.domain.model.media.CaptureImagesParams params =
                com.arassec.artivact.domain.model.media.CaptureImagesParams.builder()
                        .cameraPeripheralConfigId("camera-config-1")
                        .removeBackgrounds(false)
                        .build();

        String result = service.captureImage(itemId, params);

        assertThat(result).isEqualTo("001.jpg");
        verify(cameraPeripheral).initialize(any(ProgressMonitor.class), any(PeripheralInitParams.class));
        verify(cameraPeripheral).captureImage(any(Path.class));
        verify(cameraPeripheral).teardown();
    }

    @Test
    void captureImageCapturesWithBackgroundRemoval() {
        String itemId = "item-123";
        Path imagesDir = Path.of("/project/items/item-123/images");
        Path projectRoot = Path.of("/project");

        PeripheralConfig cameraConfig = mock(PeripheralConfig.class);
        when(cameraConfig.getId()).thenReturn("camera-config-1");
        when(cameraConfig.getPeripheralImplementation()).thenReturn(PeripheralImplementation.PTP_CAMERA_PERIPHERAL);

        PeripheralConfig bgRemovalConfig = mock(PeripheralConfig.class);
        when(bgRemovalConfig.getId()).thenReturn("bg-removal-1");
        when(bgRemovalConfig.getPeripheralImplementation()).thenReturn(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL);

        PeripheralsConfiguration peripheralsConfiguration = PeripheralsConfiguration.builder()
                .cameraPeripheralConfigs(List.of(cameraConfig))
                .imageBackgroundRemovalPeripheralConfigs(List.of(bgRemovalConfig))
                .build();

        when(loadAdapterConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(peripheralsConfiguration);
        when(useProjectDirsUseCase.getImagesDir(itemId)).thenReturn(imagesDir);
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(projectRoot);
        when(fileRepository.getNextAssetNumber(imagesDir)).thenReturn(1);
        when(fileRepository.getAssetName(1, "jpg")).thenReturn("001.jpg");
        when(cameraPeripheral.supports(PeripheralImplementation.PTP_CAMERA_PERIPHERAL)).thenReturn(true);
        when(cameraPeripheral.captureImage(any(Path.class))).thenReturn(true);
        when(imageManipulatorPeripheral.supports(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL)).thenReturn(true);
        when(imageManipulatorPeripheral.getModifiedImages()).thenReturn(List.of(Path.of("001-nobg.png")));

        com.arassec.artivact.domain.model.media.CaptureImagesParams params =
                com.arassec.artivact.domain.model.media.CaptureImagesParams.builder()
                        .cameraPeripheralConfigId("camera-config-1")
                        .imageBackgroundRemovalPeripheralConfigId("bg-removal-1")
                        .removeBackgrounds(true)
                        .build();

        String result = service.captureImage(itemId, params);

        assertThat(result).isEqualTo("001-nobg.png");
        verify(cameraPeripheral).initialize(any(ProgressMonitor.class), any(PeripheralInitParams.class));
        verify(imageManipulatorPeripheral).initialize(any(ProgressMonitor.class), any(PeripheralInitParams.class));
        verify(cameraPeripheral).captureImage(any(Path.class));
        verify(imageManipulatorPeripheral).removeBackground(any(Path.class));
        verify(cameraPeripheral).teardown();
        verify(imageManipulatorPeripheral).teardown();
    }
}
