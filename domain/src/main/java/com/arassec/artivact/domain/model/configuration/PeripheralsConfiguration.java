package com.arassec.artivact.domain.model.configuration;

import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of peripherals.
 */
@Getter
@Setter
@NoArgsConstructor
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

    /**
     * Creates a new peripherals configuration with the specified configurations and available implementations.
     */
    @SuppressWarnings("java:S107") // This constructor is required as fallback for Jackson JSON deserialization.
    public PeripheralsConfiguration(List<PeripheralConfig> turntablePeripheralConfigs,
                                    List<PeripheralImplementation> availableTurntablePeripheralImplementations,
                                    List<PeripheralConfig> cameraPeripheralConfigs,
                                    List<PeripheralImplementation> availableCameraPeripheralImplementations,
                                    List<PeripheralConfig> imageBackgroundRemovalPeripheralConfigs,
                                    List<PeripheralImplementation> availableImageManipulatorPeripheralImplementations,
                                    List<PeripheralConfig> modelCreatorPeripheralConfigs,
                                    List<PeripheralImplementation> availableModelCreatorPeripheralImplementations,
                                    List<PeripheralConfig> modelEditorPeripheralConfigs,
                                    List<PeripheralImplementation> availableModelEditorPeripheralImplementations) {
        if (turntablePeripheralConfigs != null) {
            this.turntablePeripheralConfigs = turntablePeripheralConfigs;
        }
        if (availableTurntablePeripheralImplementations != null) {
            this.availableTurntablePeripheralImplementations = availableTurntablePeripheralImplementations;
        }
        if (cameraPeripheralConfigs != null) {
            this.cameraPeripheralConfigs = cameraPeripheralConfigs;
        }
        if (availableCameraPeripheralImplementations != null) {
            this.availableCameraPeripheralImplementations = availableCameraPeripheralImplementations;
        }
        if (imageBackgroundRemovalPeripheralConfigs != null) {
            this.imageBackgroundRemovalPeripheralConfigs = imageBackgroundRemovalPeripheralConfigs;
        }
        if (availableImageManipulatorPeripheralImplementations != null) {
            this.availableImageManipulatorPeripheralImplementations = availableImageManipulatorPeripheralImplementations;
        }
        if (modelCreatorPeripheralConfigs != null) {
            this.modelCreatorPeripheralConfigs = modelCreatorPeripheralConfigs;
        }
        if (availableModelCreatorPeripheralImplementations != null) {
            this.availableModelCreatorPeripheralImplementations = availableModelCreatorPeripheralImplementations;
        }
        if (modelEditorPeripheralConfigs != null) {
            this.modelEditorPeripheralConfigs = modelEditorPeripheralConfigs;
        }
        if (availableModelEditorPeripheralImplementations != null) {
            this.availableModelEditorPeripheralImplementations = availableModelEditorPeripheralImplementations;
        }
    }

}
