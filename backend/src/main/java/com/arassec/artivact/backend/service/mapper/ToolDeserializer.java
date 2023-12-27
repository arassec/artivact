package com.arassec.artivact.backend.service.mapper;

import com.arassec.artivact.backend.service.model.exhibition.Tool;
import com.arassec.artivact.backend.service.model.exhibition.ToolType;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Map;

public class ToolDeserializer extends StdDeserializer<Tool> {

    private final ObjectMapper objectMapper;

    public ToolDeserializer() {
        super(Widget.class);
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Tool deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Map<String, Object> map = deserializationContext.readValue(jsonParser, Map.class);
        return (Tool) objectMapper.readValue(objectMapper.writeValueAsString(map), ToolType.getClassOfType(getType(map)));
    }

    /**
     * Returns the type of the component.
     *
     * @param map The component as map.
     * @return The type of the component.
     */
    private String getType(Map<String, Object> map) {
        Object typeObject = map.get("type");
        if (typeObject instanceof String type) {
            return type;
        }
        return null;
    }

}
