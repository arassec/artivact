package com.arassec.artivact.domain.misc;

import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.WidgetType;
import com.arassec.artivact.core.model.page.widget.SpaceWidget;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link WidgetDeserializer}.
 */
class WidgetDeserializerTest {

    /**
     * Tests deserializing a widget.
     */
    @Test
    @SneakyThrows
    void testDeserialize() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(jsonParserMock, Map.class)).thenReturn(Map.of(
                "type", WidgetType.SPACE.toString(),
                "size", 42
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();

        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertInstanceOf(SpaceWidget.class, widget);
        assertEquals(42, ((SpaceWidget) widget).getSize());
    }

    /**
     * Tests deserializing an unknown widget.
     */
    @Test
    @SneakyThrows
    void testDeserializeFailsafe() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(jsonParserMock, Map.class)).thenReturn(Map.of(
                "type", "UNKNOWN_WIDGET",
                "invalid", true
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();

        var widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertNull(widget);
    }

}
