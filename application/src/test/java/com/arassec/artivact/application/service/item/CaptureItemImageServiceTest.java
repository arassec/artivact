package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.application.port.out.peripheral.ImageManipulationPeripheral;
import com.arassec.artivact.application.port.out.peripheral.TurntablePeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.media.CaptureImagesParams;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptureItemImageServiceTest {

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
    private LoadPeripheralConfigurationUseCase loadPeripheralConfigurationUseCase;

    @Mock
    private CameraPeripheral cameraPeripheral;
    @Mock
    private ImageManipulationPeripheral imageManipulationPeripheral;
    @Mock
    private TurntablePeripheral turntablePeripheral;

    @Captor
    private ArgumentCaptor<BackgroundOperation> backgroundOperationArgumentCaptor;

    @InjectMocks
    private CaptureItemImageService service;

    @BeforeEach
    void setUp() {
        PeripheralConfiguration configuration = new PeripheralConfiguration();
        configuration.setCameraPeripheralImplementation(PeripheralImplementation.DEFAULT_CAMERA_PERIPHERAL);
        configuration.setImageManipulationPeripheralImplementation(PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL);
        configuration.setTurntablePeripheralImplementation(PeripheralImplementation.DEFAULT_TURNTABLE_PERIPHERAL);

        when(loadPeripheralConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(configuration);

        lenient().when(cameraPeripheral.supports(any())).thenReturn(true);
        lenient().when(imageManipulationPeripheral.supports(any())).thenReturn(true);
        lenient().when(turntablePeripheral.supports(any())).thenReturn(true);

        Path imagesDir = Path.of("target", "images");
        lenient().when(useProjectDirsUseCase.getImagesDir(anyString())).thenReturn(imagesDir);
        lenient().when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("projectRoot"));

        service = new CaptureItemImageService(
                useProjectDirsUseCase,
                runBackgroundOperationUseCase,
                loadItemUseCase,
                saveItemUseCase,
                fileRepository,
                loadPeripheralConfigurationUseCase,
                List.of(cameraPeripheral, imageManipulationPeripheral, turntablePeripheral)
        );
    }

    @Test
    void testCaptureImageWithoutBackgroundRemoval() {
        when(fileRepository.getNextAssetNumber(any())).thenReturn(1);
        when(fileRepository.getAssetName(1, "jpg")).thenReturn("img1.jpg");
        when(cameraPeripheral.captureImage(any())).thenReturn(true);

        String result = service.captureImage("item1", false);

        assertThat(result).isEqualTo("img1.jpg");
        verify(cameraPeripheral).initialize(any(), any());
        verify(cameraPeripheral).teardown();
        verify(imageManipulationPeripheral).teardown();
        verify(fileRepository).createDirIfRequired(any());
    }

    @Test
    void testCaptureImageWithBackgroundRemoval() {
        when(fileRepository.getNextAssetNumber(any())).thenReturn(1);
        when(fileRepository.getAssetName(1, "jpg")).thenReturn("img1.jpg");
        when(cameraPeripheral.captureImage(any())).thenReturn(true);
        when(imageManipulationPeripheral.getModifiedImages()).thenReturn(List.of(Path.of("target/images/img1-mod.jpg")));

        String result = service.captureImage("item1", true);

        // Background of the original image is removed:
        assertThat(result).isEqualTo("img1-mod.jpg");
        ArgumentCaptor<Path> argCap = ArgumentCaptor.forClass(Path.class);
        verify(imageManipulationPeripheral).removeBackground(argCap.capture());
        assertThat(argCap.getValue().toString()).endsWith("img1.jpg");

        // The original image is deleted afterward:
        argCap = ArgumentCaptor.forClass(Path.class);
        verify(fileRepository).delete(argCap.capture());
        assertThat(argCap.getValue().toString()).endsWith("img1.jpg");
    }

    @Test
    void testCaptureImageFailsIfCameraFails() {
        when(fileRepository.getNextAssetNumber(any())).thenReturn(1);
        when(fileRepository.getAssetName(1, "jpg")).thenReturn("img1.jpg");
        when(cameraPeripheral.captureImage(any())).thenReturn(false);

        String result = service.captureImage("item1", false);

        assertThat(result).isEqualTo("img1.jpg"); // file is not deleted
        verify(cameraPeripheral).teardown();
    }

    @Test
    void testCaptureImagesWithTurntableAndBackgroundRemoval() {
        CaptureImagesParams params = new CaptureImagesParams();
        params.setNumPhotos(2);
        params.setUseTurnTable(true);
        params.setRemoveBackgrounds(true);

        when(fileRepository.getNextAssetNumber(any())).thenReturn(1, 2);
        when(fileRepository.getAssetName(1, "jpg")).thenReturn("img1.jpg");
        when(fileRepository.getAssetName(2, "jpg")).thenReturn("img2.jpg");
        when(cameraPeripheral.captureImage(any())).thenReturn(true);
        when(imageManipulationPeripheral.getModifiedImages()).thenReturn(List.of(Path.of("target/images/img1-mod.jpg")));

        Item item = new Item();
        item.setId("item1");
        when(loadItemUseCase.loadTranslated("item1")).thenReturn(item);

        service.captureImages("item1", params);

        verify(runBackgroundOperationUseCase).execute(eq("captureImages"), eq("start"), backgroundOperationArgumentCaptor.capture());

        backgroundOperationArgumentCaptor.getValue().execute(new ProgressMonitor("captureImages", "start"));

        verify(turntablePeripheral, times(2)).rotate(anyInt());
        verify(saveItemUseCase).save(any(Item.class));
    }

    @Test
    void testCaptureThrowsIfNoPeripheralFound() {
        service = new CaptureItemImageService(
                useProjectDirsUseCase,
                runBackgroundOperationUseCase,
                loadItemUseCase,
                saveItemUseCase,
                fileRepository,
                loadPeripheralConfigurationUseCase,
                List.of() // no adapters
        );

        assertThatThrownBy(() ->
                service.captureImage("item1", false)
        ).isInstanceOf(ArtivactException.class);
    }

}
