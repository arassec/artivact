package com.arassec.artivact.adapter.out.model.editor.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.configs.ExternalProgramPeripheralConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ExternalProgramModelEditorPeripheral}.
 */
@ExtendWith(MockitoExtension.class)
class ExternalProgramModelEditorPeripheralTest {

    /**
     * Peripheral under test.
     */
    @InjectMocks
    private ExternalProgramModelEditorPeripheral peripheral;

    /**
     * Gateway to the operating system.
     */
    @Mock
    private OsGateway osGateway;

    /**
     * Argument captor for the blender command.
     */
    @Captor
    private ArgumentCaptor<String> commandCaptor;

    /**
     * Argument captor for command line parameters.
     */
    @Captor
    private ArgumentCaptor<List<String>> commandParamsCaptor;

    /**
     * Tests getting the peripheral implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(peripheral.getSupportedImplementation()).isEqualTo(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL);
    }

    /**
     * Tests opening a model without an existing blender project.
     */
    @Test
    @EnabledOnOs(OS.LINUX)
    void testOpenProject() {
        ProgressMonitor progressMonitor = new ProgressMonitor("BlenderModelEditorPeripheralTest", "testOpen");
        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .projectRoot(Path.of("src/test/resources"))
                .config(new ExternalProgramPeripheralConfig("/path/to/blender-executable",
                        "--python utils/Blender/blender-artivact-import.py\n-- {modelDir}"))
                .build();

        peripheral.initialize(progressMonitor, initParams);

        peripheral.open(CreationModelSet.builder()
                .directory("")
                .build());

        verify(osGateway).execute(commandCaptor.capture(), commandParamsCaptor.capture());

        assertThat(commandCaptor.getValue()).isEqualTo("/path/to/blender-executable");

        List<String> commandParams = commandParamsCaptor.getValue();
        assertThat(commandParams).hasSize(4);
        assertThat(commandParams.get(0)).isEqualTo("--python");
        assertThat(commandParams.get(1)).endsWith("utils/Blender/blender-artivact-import.py");
        assertThat(commandParams.get(2)).isEqualTo("--");
        assertThat(commandParams.get(3)).endsWith("src/test/resources");
    }

    /**
     * Tests getStatus when the configured command is not executable.
     */
    @Test
    void testGetStatusNotExecutable() {
        ExternalProgramPeripheralConfig config = new ExternalProgramPeripheralConfig();
        config.setCommand("/non/existent");
        when(osGateway.isExecutable("/non/existent")).thenReturn(false);

        assertThat(peripheral.getStatus(config).name()).isEqualTo("NOT_EXECUTABLE");
    }

    /**
     * Tests getStatus when the peripheral is in use (should be AVAILABLE).
     */
    @Test
    void testGetStatusInUse() throws Exception {
        // set inUse = true via reflection
        Field inUseField = peripheral.getClass().getSuperclass().getDeclaredField("inUse");
        inUseField.setAccessible(true);
        inUseField.set(peripheral, new AtomicBoolean(true));

        ExternalProgramPeripheralConfig config = new ExternalProgramPeripheralConfig();
        config.setCommand("/does/not/matter");

        assertThat(peripheral.getStatus(config)).isEqualTo(com.arassec.artivact.domain.model.peripheral.PeripheralStatus.AVAILABLE);
    }

    /**
     * Tests scanPeripherals returns found blender installation on Linux.
     */
    @Test
    @EnabledOnOs(OS.LINUX)
    void testScanPeripheralsLinuxFound() {
        when(osGateway.isLinux()).thenReturn(true);
        Path home = Path.of("/home/testuser");
        when(osGateway.getUserHomeDirectory()).thenReturn(home);
        when(osGateway.scanForDirectory(home, 5, "blender-4.5"))
                .thenReturn(Optional.of(home.resolve("blender-4.5")));

        List<?> result = peripheral.scanPeripherals();

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst()).hasFieldOrProperty("label");
        assertThat(result.getFirst().toString()).contains("Blender");
    }

    /**
     * Tests scanPeripherals returns empty list when peripheral is in use.
     */
    @Test
    void testScanPeripheralsInUseReturnsEmpty() throws Exception {
        // set inUse = true via reflection
        Field inUseField = peripheral.getClass().getSuperclass().getDeclaredField("inUse");
        inUseField.setAccessible(true);
        inUseField.set(peripheral, new AtomicBoolean(true));

        List<?> result = peripheral.scanPeripherals();
        assertThat(result).isEmpty();
    }

    /**
     * Tests open() when no arguments are configured (arguments == null).
     */
    @Test
    void testOpenNoArguments() {
        ProgressMonitor progressMonitor = new ProgressMonitor("BlenderModelEditorPeripheralTest", "testOpenNoArgs");
        ExternalProgramPeripheralConfig config = new ExternalProgramPeripheralConfig();
        config.setCommand("/path/to/executable");
        config.setArguments(null);

        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .projectRoot(Path.of("some/project"))
                .config(config)
                .build();

        peripheral.initialize(progressMonitor, initParams);

        peripheral.open(CreationModelSet.builder().directory("models").build());

        verify(osGateway).execute(commandCaptor.capture(), commandParamsCaptor.capture());
        assertThat(commandCaptor.getValue()).isEqualTo("/path/to/executable");
        List<String> params = commandParamsCaptor.getValue();
        assertThat(params).isEmpty();
    }

}
