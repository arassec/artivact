package com.arassec.artivact.application.infrastructure.mapper;

import com.arassec.artivact.domain.model.peripheral.configs.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Map;

public class PeripheralConfigDeserializer extends StdDeserializer<PeripheralConfig> {

    /**
     * The object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance.
     */
    public PeripheralConfigDeserializer() {
        super(PeripheralConfig.class);
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public PeripheralConfig deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Map<String, Object> map = deserializationContext.readValue(jsonParser, Map.class);
        Class<?> classOfType = getClassOfImplementation(getPeripheralImplementation(map));
        if (classOfType == null) {
            return null;
        }
        return (PeripheralConfig) objectMapper.readValue(objectMapper.writeValueAsString(map), classOfType);
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
