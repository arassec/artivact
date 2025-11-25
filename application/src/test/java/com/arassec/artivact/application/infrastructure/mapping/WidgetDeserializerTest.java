package com.arassec.artivact.application.infrastructure.mapping;

import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.WidgetType;
import com.arassec.artivact.domain.model.page.widget.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link WidgetDeserializer}.
 */
@SuppressWarnings("unchecked")
class WidgetDeserializerTest {

    /**
     * Tests deserializing the {@link com.arassec.artivact.domain.model.page.widget.PageTitleWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializePageTitleWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(Map.of(
                "type", WidgetType.PAGE_TITLE.toString()
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();
        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertInstanceOf(PageTitleWidget.class, widget);
    }

    /**
     * Tests deserializing the {@link com.arassec.artivact.domain.model.page.widget.TextWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeTextWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(Map.of(
                "type", WidgetType.TEXT.toString()
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();
        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertInstanceOf(TextWidget.class, widget);
    }

    /**
     * Tests deserializing the {@link com.arassec.artivact.domain.model.page.widget.ItemSearchWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeItemSearchWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(Map.of(
                "type", WidgetType.ITEM_SEARCH.toString(),
                "searchTerm", "*"
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();

        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertInstanceOf(ItemSearchWidget.class, widget);
        assertThat(((ItemSearchWidget) widget).getHeading()).isNotNull();
        assertThat(((ItemSearchWidget) widget).getContent()).isNotNull();
    }

    /**
     * Tests deserializing the {@link com.arassec.artivact.domain.model.page.widget.InfoBoxWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeInfoBoxWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(Map.of(
                "type", WidgetType.INFO_BOX.toString()
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();
        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertInstanceOf(InfoBoxWidget.class, widget);
    }

    /**
     * Tests deserializing the {@link com.arassec.artivact.domain.model.page.widget.AvatarWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeAvatarWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(Map.of(
                "type", WidgetType.AVATAR.toString()
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();
        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertInstanceOf(AvatarWidget.class, widget);
    }

    /**
     * Tests deserializing the {@link ImageGalleryWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeImageGalleryWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(Map.of(
                "type", WidgetType.IMAGE_GALLERY.toString()
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();
        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertInstanceOf(ImageGalleryWidget.class, widget);
    }

    /**
     * Tests deserializing an unknown widget.
     */
    @Test
    @SneakyThrows
    void testDeserializeFailsafe() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(eq(jsonParserMock), any(TypeReference.class))).thenReturn(Map.of(
                "type", "UNKNOWN_WIDGET",
                "invalid", true
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();

        var widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertNull(widget);
    }

}
