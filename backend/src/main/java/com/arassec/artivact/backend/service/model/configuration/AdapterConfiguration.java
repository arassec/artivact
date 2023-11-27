package com.arassec.artivact.backend.service.model.configuration;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class AdapterConfiguration {

    private AdapterImplementation backgroundRemovalAdapterImplementation;

    private AdapterImplementation cameraAdapterImplementation;

    private AdapterImplementation turntableAdapterImplementation;

    private AdapterImplementation modelCreatorImplementation;

    private AdapterImplementation modelEditorImplementation;

    @Builder.Default
    private Map<AdapterImplementation, String> configValues = new HashMap<>();

    public String getConfigValue(AdapterImplementation adapterImplementation) {
        return configValues.get(adapterImplementation);
    }

}
