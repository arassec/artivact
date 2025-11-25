package com.arassec.artivact.domain.model.configuration;

import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import lombok.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of peripherals.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeripheralsConfiguration {

    /**
     * The configured turntable peripherals.
     */
    @Builder.Default
    private List<PeripheralConfig> turntablePeripheralConfigs = new LinkedList<>();

    /**
     * All available turntable peripheral implementations.
     */
    @Builder.Default
    private List<PeripheralImplementation> availableTurntablePeripheralImplementations = new LinkedList<>();

    /**
     * The configured camera peripherals.
     */
    @Builder.Default
    private List<PeripheralConfig> cameraPeripheralConfigs = new LinkedList<>();

    /**
     * All available camera adapters.
     */
    @Builder.Default
    private List<PeripheralImplementation> availableCameraPeripheralImplementations = new LinkedList<>();

    /**
     * The configured image-manipulators.
     */
    @Builder.Default
    private List<PeripheralConfig> imageBackgroundRemovalPeripheralConfigs = new LinkedList<>();

    /**
     * All available image-manipulation adapters.
     */
    @Builder.Default
    private List<PeripheralImplementation> availableImageManipulatorPeripheralImplementations = new LinkedList<>();

    /**
     * The current model creator adapter.
     */
    @Builder.Default
    private List<PeripheralConfig> modelCreatorPeripheralConfigs = new LinkedList<>();

    /**
     * All available model creator adapters.
     */
    @Builder.Default
    private List<PeripheralImplementation> availableModelCreatorPeripheralImplementations = new LinkedList<>();

    /**
     * The current model editor adapter.
     */
    @Builder.Default
    private List<PeripheralConfig> modelEditorPeripheralConfigs = new LinkedList<>();

    /**
     * All available model editor adapters.
     */
    @Builder.Default
    private List<PeripheralImplementation> availableModelEditorPeripheralImplementations = new LinkedList<>();

}
