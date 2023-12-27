package com.arassec.artivact.backend.service.model.configuration;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdapterConfiguration {

    private AdapterImplementation backgroundRemovalAdapterImplementation;

    private List<AdapterImplementation> availableBackgroundRemovalAdapterImplementations = new LinkedList<>();

    private AdapterImplementation cameraAdapterImplementation;

    private List<AdapterImplementation> availableCameraAdapterImplementations = new LinkedList<>();

    private AdapterImplementation turntableAdapterImplementation;

    private List<AdapterImplementation> availableTurntableAdapterImplementations = new LinkedList<>();

    private AdapterImplementation modelCreatorImplementation;

    private List<AdapterImplementation> availableModelCreatorAdapterImplementations = new LinkedList<>();

    private AdapterImplementation modelEditorImplementation;

    private List<AdapterImplementation> availableModelEditorAdapterImplementations = new LinkedList<>();

    private Map<AdapterImplementation, String> configValues = new HashMap<>();

    public String getConfigValue(AdapterImplementation adapterImplementation) {
        return configValues.get(adapterImplementation);
    }

}
