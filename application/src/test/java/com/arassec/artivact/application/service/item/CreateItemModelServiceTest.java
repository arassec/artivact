package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ModelCreatorPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.media.CreateModelParams;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateItemModelServiceTest {

    private CreateItemModelService service;

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
    private ModelCreatorPeripheral modelCreatorPeripheral;

    @BeforeEach
    void setUp() {
        List<Peripheral> peripherals = new ArrayList<>();
        peripherals.add(modelCreatorPeripheral);

        service = new CreateItemModelService(
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
    void createModelReturnsEmptyWhenNoSourceDirectory() {
        String itemId = "item-123";
        Path tempDir = Path.of("/project/temp");
        Path projectRoot = Path.of("/project");
        Path imagesDir = Path.of("/project/items/item-123/images");

        PeripheralConfig modelCreatorConfig = mock(PeripheralConfig.class);
        when(modelCreatorConfig.getId()).thenReturn("creator-1");
        when(modelCreatorConfig.getPeripheralImplementation()).thenReturn(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL);

        PeripheralsConfiguration peripheralsConfiguration = PeripheralsConfiguration.builder()
                .modelCreatorPeripheralConfigs(List.of(modelCreatorConfig))
                .build();

        when(loadAdapterConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(peripheralsConfiguration);
        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(projectRoot);
        when(useProjectDirsUseCase.getImagesDir(itemId)).thenReturn(imagesDir);
        when(modelCreatorPeripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL)).thenReturn(true);

        Path resultDir = Path.of("/project/temp/result");
        when(modelCreatorPeripheral.createModel(any())).thenReturn(new ModelCreationResult(resultDir, "comment"));
        when(fileRepository.exists(resultDir)).thenReturn(false);

        CreateModelParams params = CreateModelParams.builder()
                .modelCreatorPeripheralConfigId("creator-1")
                .build();

        CreationImageSet imageSet = CreationImageSet.builder()
                .modelInput(true)
                .files(List.of("001.jpg", "002.jpg"))
                .build();

        ProgressMonitor progressMonitor = new ProgressMonitor("test", "test");

        Optional<CreationModelSet> result = service.createModel(itemId, params, List.of(imageSet), progressMonitor);

        assertThat(result).isEmpty();
        verify(modelCreatorPeripheral).initialize(eq(progressMonitor), any(PeripheralInitParams.class));
        verify(modelCreatorPeripheral).teardown();
    }

    @Test
    void createModelReturnsEmptyWhenNoFilesInResultDir() {
        String itemId = "item-123";
        Path tempDir = Path.of("/project/temp");
        Path projectRoot = Path.of("/project");
        Path imagesDir = Path.of("/project/items/item-123/images");
        Path modelsDir = Path.of("/project/items/item-123/models");

        PeripheralConfig modelCreatorConfig = mock(PeripheralConfig.class);
        when(modelCreatorConfig.getId()).thenReturn("creator-1");
        when(modelCreatorConfig.getPeripheralImplementation()).thenReturn(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL);

        PeripheralsConfiguration peripheralsConfiguration = PeripheralsConfiguration.builder()
                .modelCreatorPeripheralConfigs(List.of(modelCreatorConfig))
                .build();

        when(loadAdapterConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(peripheralsConfiguration);
        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(projectRoot);
        when(useProjectDirsUseCase.getImagesDir(itemId)).thenReturn(imagesDir);
        when(useProjectDirsUseCase.getModelsDir(itemId)).thenReturn(modelsDir);
        when(modelCreatorPeripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL)).thenReturn(true);

        Path resultDir = Path.of("/project/temp/result");
        when(modelCreatorPeripheral.createModel(any())).thenReturn(new ModelCreationResult(resultDir, "comment"));
        when(fileRepository.exists(resultDir)).thenReturn(true);
        when(fileRepository.getNextAssetNumber(modelsDir)).thenReturn(1);
        when(fileRepository.getAssetName(1, null)).thenReturn("001");
        when(fileRepository.list(resultDir)).thenReturn(List.of());

        CreateModelParams params = CreateModelParams.builder()
                .modelCreatorPeripheralConfigId("creator-1")
                .build();

        CreationImageSet imageSet = CreationImageSet.builder()
                .modelInput(true)
                .files(List.of("001.jpg"))
                .build();

        ProgressMonitor progressMonitor = new ProgressMonitor("test", "test");

        Optional<CreationModelSet> result = service.createModel(itemId, params, List.of(imageSet), progressMonitor);

        assertThat(result).isEmpty();
    }
}
