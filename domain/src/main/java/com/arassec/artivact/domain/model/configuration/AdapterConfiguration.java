package com.arassec.artivact.domain.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Configuration of adapters.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdapterConfiguration {

    /**
     * The current image-manipulation adapter.
     */
    private AdapterImplementation imageManipulationAdapterImplementation;

    /**
     * All available image-manipulation adapters.
     */
    private List<AdapterImplementation> availableImageManipulationAdapterImplementations = new LinkedList<>();

    /**
     * The current camera adapter.
     */
    private AdapterImplementation cameraAdapterImplementation;

    /**
     * All available camera adapters.
     */
    private List<AdapterImplementation> availableCameraAdapterImplementations = new LinkedList<>();

    /**
     * The current turntable adapter.
     */
    private AdapterImplementation turntableAdapterImplementation;

    /**
     * All available turntable adapters.
     */
    private List<AdapterImplementation> availableTurntableAdapterImplementations = new LinkedList<>();

    /**
     * The current model creator adapter.
     */
    private AdapterImplementation modelCreatorImplementation;

    /**
     * All available model creator adapters.
     */
    private List<AdapterImplementation> availableModelCreatorAdapterImplementations = new LinkedList<>();

    /**
     * The current model editor adapter.
     */
    private AdapterImplementation modelEditorImplementation;

    /**
     * All available model editor adapters.
     */
    private List<AdapterImplementation> availableModelEditorAdapterImplementations = new LinkedList<>();

    /**
     * The configuration values per adapter.
     */
    private Map<AdapterImplementation, String> configValues = new EnumMap<>(AdapterImplementation.class);

    /**
     * Returns the config value for the given adapter.
     *
     * @param adapterImplementation The adapter.
     * @return The configured value for this adapter.
     */
    public String getConfigValue(AdapterImplementation adapterImplementation) {
        return configValues.get(adapterImplementation);
    }

}
