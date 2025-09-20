package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ModelCreatorPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateItemModelServiceTest {

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
    private ModelCreatorPeripheral modelCreatorPeripheral;

    @InjectMocks
    private CreateItemModelService service;

    private final Path tempDir = Path.of("target", "temp");
    private final Path projectRoot = Path.of("target", "project");
    private final Path imagesDir = Path.of("target", "images");
    private final Path modelsDir = Path.of("target", "models");

    @BeforeEach
    void setUp() {
        PeripheralConfiguration config = new PeripheralConfiguration();
        config.setModelCreatorPeripheralImplementation(PeripheralImplementation.FALLBACK_MODEL_CREATOR_PERIPHERAL);

        when(loadPeripheralConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(config);
        lenient().when(modelCreatorPeripheral.supports(any())).thenReturn(true);

        lenient().when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);
        lenient().when(useProjectDirsUseCase.getProjectRoot()).thenReturn(projectRoot);
        lenient().when(useProjectDirsUseCase.getImagesDir(anyString())).thenReturn(imagesDir);
        lenient().when(useProjectDirsUseCase.getModelsDir(anyString())).thenReturn(modelsDir);

        service = new CreateItemModelService(
                useProjectDirsUseCase,
                runBackgroundOperationUseCase,
                loadItemUseCase,
                saveItemUseCase,
                fileRepository,
                loadPeripheralConfigurationUseCase,
                List.of(modelCreatorPeripheral)
        );
    }

    @Test
    void testCreateModelSuccessfully() {
        Path sourceDir = Path.of("model-source");
        Path file = sourceDir.resolve("file.obj");

        CreationImageSet imageSet = CreationImageSet.builder().modelInput(true).build();
        imageSet.getFiles().add("file1.jpg");

        Path itemModelsDir = Path.of("items/012/abc/012abc/models/");
        when(useProjectDirsUseCase.getModelsDir("item1")).thenReturn(itemModelsDir);

        when(fileRepository.list(sourceDir)).thenReturn(List.of(file));
        when(fileRepository.exists(sourceDir)).thenReturn(true);
        when(fileRepository.isDir(any())).thenReturn(false);
        when(fileRepository.getNextAssetNumber(itemModelsDir)).thenReturn(1);
        when(fileRepository.getAssetName(1, null)).thenReturn("001");

        ModelCreationResult result = new ModelCreationResult(sourceDir, "ok");
        when(modelCreatorPeripheral.createModel(any())).thenReturn(result);

        Optional<CreationModelSet> modelSetOpt = service.createModel("item1", List.of(imageSet), new ProgressMonitor("test", "test"));

        assertThat(modelSetOpt).isPresent();
        assertThat(modelSetOpt.get().getComment()).isEqualTo("ok");
        verify(modelCreatorPeripheral).initialize(any(), any());
        verify(modelCreatorPeripheral).teardown();
    }

    @Test
    void testCreateModelReturnsEmptyIfSourceDirDoesNotExist() {
        Path nonExisting = Path.of("non-existing-dir");
        ModelCreationResult result = new ModelCreationResult(nonExisting, "none");
        when(modelCreatorPeripheral.createModel(any())).thenReturn(result);

        Optional<CreationModelSet> modelSetOpt = service.createModel("item1", List.of(), new ProgressMonitor("test", "test"));

        assertThat(modelSetOpt).isEmpty();
    }

    @Test
    void testThrowsIfSourceDirEmpty() {
        Path sourceDir = Path.of("empty-dir");
        ModelCreationResult result = new ModelCreationResult(sourceDir, "empty");
        when(modelCreatorPeripheral.createModel(any())).thenReturn(result);
        when(fileRepository.exists(sourceDir)).thenReturn(true);
        when(fileRepository.list(sourceDir)).thenReturn(List.of());

        when(fileRepository.getNextAssetNumber(modelsDir)).thenReturn(1);
        when(fileRepository.getAssetName(1, null)).thenReturn("001");

        List<CreationImageSet> imageSets = new ArrayList<>();
        ProgressMonitor progressMonitor = new ProgressMonitor("test", "test");

        assertThatThrownBy(() -> service.createModel("item1", imageSets, progressMonitor))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No model files found");
        verify(fileRepository).delete(any());
    }

    @Test
    void testThrowsIfCopyFails() {
        Path sourceDir = Path.of("source-dir");
        Path file = sourceDir.resolve("file.obj");

        ModelCreationResult result = new ModelCreationResult(sourceDir, "copyFail");
        when(modelCreatorPeripheral.createModel(any())).thenReturn(result);
        when(fileRepository.list(sourceDir)).thenReturn(List.of(file));
        when(fileRepository.isDir(file)).thenReturn(false);
        when(fileRepository.exists(sourceDir)).thenReturn(true);

        when(fileRepository.getNextAssetNumber(modelsDir)).thenReturn(1);
        when(fileRepository.getAssetName(1, null)).thenReturn("001");

        doThrow(new ArtivactException("copy fail")).when(fileRepository)
                .copy(any(Path.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING));


        List<CreationImageSet> list = List.of();
        ProgressMonitor progressMonitor = new ProgressMonitor("test", "test");

        assertThatThrownBy(() -> service.createModel("item1", list, progressMonitor))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not copy model files");
        verify(fileRepository).delete(any());
    }

    @Test
    void testThrowsIfNoAdapterFound() {
        service = new CreateItemModelService(
                useProjectDirsUseCase,
                runBackgroundOperationUseCase,
                loadItemUseCase,
                saveItemUseCase,
                fileRepository,
                loadPeripheralConfigurationUseCase,
                List.of() // no adapters
        );

        List<CreationImageSet> list = List.of();
        ProgressMonitor progressMonitor = new ProgressMonitor("test", "test");

        assertThatThrownBy(() -> service.createModel("item1", list, progressMonitor))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not detect selected model-creator adapter");
    }

}
