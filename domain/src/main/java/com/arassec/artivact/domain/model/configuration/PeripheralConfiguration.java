package com.arassec.artivact.domain.model.configuration;

import lombok.*;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Configuration of peripherals.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeripheralConfiguration {

    /**
     * The current image-manipulation adapter.
     */
    private PeripheralImplementation imageManipulationPeripheralImplementation;

    /**
     * All available image-manipulation adapters.
     */
    private List<PeripheralImplementation> availableImageManipulationPeripheralImplementations = new LinkedList<>();

    /**
     * The current camera adapter.
     */
    private PeripheralImplementation cameraPeripheralImplementation;

    /**
     * All available camera adapters.
     */
    private List<PeripheralImplementation> availableCameraPeripheralImplementations = new LinkedList<>();

    /**
     * The current turntable adapter.
     */
    private PeripheralImplementation turntablePeripheralImplementation;

    /**
     * All available turntable adapters.
     */
    private List<PeripheralImplementation> availableTurntablePeripheralImplementations = new LinkedList<>();

    /**
     * The current model creator adapter.
     */
    private PeripheralImplementation modelCreatorPeripheralImplementation;

    /**
     * All available model creator adapters.
     */
    private List<PeripheralImplementation> availableModelCreatorPeripheralImplementations = new LinkedList<>();

    /**
     * The current model editor adapter.
     */
    private PeripheralImplementation modelEditorPeripheralImplementation;

    /**
     * All available model editor adapters.
     */
    private List<PeripheralImplementation> availableModelEditorPeripheralImplementations = new LinkedList<>();

    /**
     * The configuration values per adapter.
     */
    private Map<PeripheralImplementation, String> configValues = new EnumMap<>(PeripheralImplementation.class);

    /**
     * Returns the config value for the given adapter.
     *
     * @param adapterImplementation The adapter.
     * @return The configured value for this adapter.
     */
    public String getConfigValue(PeripheralImplementation adapterImplementation) {
        return configValues.get(adapterImplementation);
    }

}
