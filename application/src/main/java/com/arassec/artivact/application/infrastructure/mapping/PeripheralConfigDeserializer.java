package com.arassec.artivact.application.infrastructure.mapping;

import com.arassec.artivact.domain.model.peripheral.configs.*;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * Custom {@link ValueDeserializer} deserializer for Artivact peripheral configs.
 */
public class PeripheralConfigDeserializer extends ValueDeserializer<PeripheralConfig> {

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();

    @Override
    public PeripheralConfig deserialize(JsonParser jsonParser, tools.jackson.databind.DeserializationContext context) throws JacksonException {
        Map<String, Object> map = context.readValue(jsonParser, new TypeReference<>() {
        });
        Class<?> classOfType = getClassOfImplementation(getPeripheralImplementation(map));
        if (classOfType == null) {
            return null;
        }
        return (PeripheralConfig) jsonMapper.readValue(jsonMapper.writeValueAsString(map), classOfType);
    }

    /**
     * Returns the type of the config.
     *
     * @param map The component as map.
     * @return The type of the component.
     */
    private String getPeripheralImplementation(Map<String, Object> map) {
        Object typeObject = map.get("peripheralImplementation");
        if (typeObject instanceof String type) {
            return type;
        }
        return null;
    }

    /**
     * Returns a peripheral's config class for its implementation.
     *
     * @param peripheralImplementation The peripheral's implementation.
     * @return The class of the peripheral config.
     */
    private Class<? extends PeripheralConfig> getClassOfImplementation(String peripheralImplementation) {
        if (peripheralImplementation == null) {
            return null;
        }
        switch (peripheralImplementation) {
            case "ARDUINO_TURNTABLE_PERIPHERAL" -> {
                return ArduinoTurntablePeripheralConfig.class;
            }
            case "ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL" -> {
                return OnnxBackgroundRemovalPeripheralConfig.class;
            }
            case "PTP_CAMERA_PERIPHERAL" -> {
                return PtpCameraPeripheralConfig.class;
            }
            case "EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL" -> {
                return ModelCreatorPeripheralConfig.class;
            }
            case "EXTERNAL_PROGRAM_CAMERA_PERIPHERAL",
                 "EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL" -> {
                return ExternalProgramPeripheralConfig.class;
            }
            default -> {
                return null;
            }
        }
    }


}
