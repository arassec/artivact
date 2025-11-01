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

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

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

}
