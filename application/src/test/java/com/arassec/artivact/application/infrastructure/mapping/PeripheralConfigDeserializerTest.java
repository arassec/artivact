package com.arassec.artivact.application.infrastructure.mapping;

import com.arassec.artivact.domain.model.peripheral.configs.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link PeripheralConfigDeserializer}.
 */
@SuppressWarnings("unchecked")
class PeripheralConfigDeserializerTest {

    /**
     * Tests deserializing the {@link ArduinoTurntablePeripheralConfig}.
     */
    @Test
    @SneakyThrows
    void testDeserializeArduinoTurntablePeripheralConfig() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        Map<String, Object> map = new HashMap<>();
        map.put("peripheralImplementation", "ARDUINO_TURNTABLE_PERIPHERAL");
        map.put("delayInMilliseconds", 100);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(map);

        PeripheralConfigDeserializer deserializer = new PeripheralConfigDeserializer();
        PeripheralConfig config = deserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertInstanceOf(ArduinoTurntablePeripheralConfig.class, config);
        assertThat(((ArduinoTurntablePeripheralConfig) config).getDelayInMilliseconds()).isEqualTo(100);
    }

    /**
     * Tests deserializing the {@link OnnxBackgroundRemovalPeripheralConfig}.
     */
    @Test
    @SneakyThrows
    void testDeserializeOnnxBackgroundRemovalPeripheralConfig() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        Map<String, Object> map = new HashMap<>();
        map.put("peripheralImplementation", "ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL");
        map.put("imageWidth", 512);
        map.put("imageHeight", 512);
        map.put("numThreads", 4);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(map);

        PeripheralConfigDeserializer deserializer = new PeripheralConfigDeserializer();
        PeripheralConfig config = deserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertInstanceOf(OnnxBackgroundRemovalPeripheralConfig.class, config);
        assertThat(((OnnxBackgroundRemovalPeripheralConfig) config).getImageWidth()).isEqualTo(512);
    }

    /**
     * Tests deserializing the {@link PtpCameraPeripheralConfig}.
     */
    @Test
    @SneakyThrows
    void testDeserializePtpCameraPeripheralConfig() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        Map<String, Object> map = new HashMap<>();
        map.put("peripheralImplementation", "PTP_CAMERA_PERIPHERAL");
        map.put("delayInMilliseconds", 100L);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(map);

        PeripheralConfigDeserializer deserializer = new PeripheralConfigDeserializer();
        PeripheralConfig config = deserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertInstanceOf(PtpCameraPeripheralConfig.class, config);
        assertThat(((PtpCameraPeripheralConfig) config).getDelayInMilliseconds()).isEqualTo(100L);
    }

    /**
     * Tests deserializing the {@link ModelCreatorPeripheralConfig}.
     */
    @Test
    @SneakyThrows
    void testDeserializeModelCreatorPeripheralConfig() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        Map<String, Object> map = new HashMap<>();
        map.put("peripheralImplementation", "EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL");
        map.put("openInputDirInOs", true);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(map);

        PeripheralConfigDeserializer deserializer = new PeripheralConfigDeserializer();
        PeripheralConfig config = deserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertInstanceOf(ModelCreatorPeripheralConfig.class, config);
        assertThat(((ModelCreatorPeripheralConfig) config).isOpenInputDirInOs()).isTrue();
    }

    /**
     * Tests deserializing the {@link ExternalProgramPeripheralConfig} for camera.
     */
    @Test
    @SneakyThrows
    void testDeserializeExternalProgramCameraPeripheralConfig() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        Map<String, Object> map = new HashMap<>();
        map.put("peripheralImplementation", "EXTERNAL_PROGRAM_CAMERA_PERIPHERAL");

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(map);

        PeripheralConfigDeserializer deserializer = new PeripheralConfigDeserializer();
        PeripheralConfig config = deserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertInstanceOf(ExternalProgramPeripheralConfig.class, config);
    }

    /**
     * Tests deserializing the {@link ExternalProgramPeripheralConfig} for model editor.
     */
    @Test
    @SneakyThrows
    void testDeserializeExternalProgramModelEditorPeripheralConfig() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        Map<String, Object> map = new HashMap<>();
        map.put("peripheralImplementation", "EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL");

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(map);

        PeripheralConfigDeserializer deserializer = new PeripheralConfigDeserializer();
        PeripheralConfig config = deserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertInstanceOf(ExternalProgramPeripheralConfig.class, config);
    }

    /**
     * Tests deserializing with an unknown peripheral implementation returns null.
     */
    @Test
    @SneakyThrows
    void testDeserializeUnknownPeripheralImplementation() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        Map<String, Object> map = new HashMap<>();
        map.put("peripheralImplementation", "UNKNOWN_PERIPHERAL");

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(map);

        PeripheralConfigDeserializer deserializer = new PeripheralConfigDeserializer();
        PeripheralConfig config = deserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertNull(config);
    }

    /**
     * Tests deserializing with null peripheral implementation returns null.
     */
    @Test
    @SneakyThrows
    void testDeserializeNullPeripheralImplementation() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        Map<String, Object> map = new HashMap<>();

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(map);

        PeripheralConfigDeserializer deserializer = new PeripheralConfigDeserializer();
        PeripheralConfig config = deserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertNull(config);
    }

    /**
     * Tests deserializing with non-string peripheral implementation returns null.
     */
    @Test
    @SneakyThrows
    void testDeserializeNonStringPeripheralImplementation() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        Map<String, Object> map = new HashMap<>();
        map.put("peripheralImplementation", 12345);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(map);

        PeripheralConfigDeserializer deserializer = new PeripheralConfigDeserializer();
        PeripheralConfig config = deserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertNull(config);
    }

}
