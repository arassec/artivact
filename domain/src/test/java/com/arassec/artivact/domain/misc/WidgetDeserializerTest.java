package com.arassec.artivact.domain.misc;

import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.WidgetType;
import com.arassec.artivact.core.model.page.widget.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link WidgetDeserializer}.
 */
class WidgetDeserializerTest {

    /**
     * Tests deserializing the {@link com.arassec.artivact.core.model.page.widget.PageTitleWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializePageTitleWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(jsonParserMock, Map.class)).thenReturn(Map.of(
                "type", WidgetType.PAGE_TITLE.toString()
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();
        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertInstanceOf(PageTitleWidget.class, widget);
    }

    /**
     * Tests deserializing the {@link com.arassec.artivact.core.model.page.widget.TextWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeTextWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(jsonParserMock, Map.class)).thenReturn(Map.of(
                "type", WidgetType.TEXT.toString()
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();
        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertInstanceOf(TextWidget.class, widget);
    }

    /**
     * Tests deserializing the {@link com.arassec.artivact.core.model.page.widget.ItemSearchWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeItemSearchWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(jsonParserMock, Map.class)).thenReturn(Map.of(
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
     * Tests deserializing the {@link com.arassec.artivact.core.model.page.widget.InfoBoxWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeInfoBoxWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(jsonParserMock, Map.class)).thenReturn(Map.of(
                "type", WidgetType.INFO_BOX.toString()
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();
        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertInstanceOf(InfoBoxWidget.class, widget);
    }

    /**
     * Tests deserializing the {@link com.arassec.artivact.core.model.page.widget.AvatarWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeAvatarWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(jsonParserMock, Map.class)).thenReturn(Map.of(
                "type", WidgetType.AVATAR.toString()
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();
        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertInstanceOf(AvatarWidget.class, widget);
    }

    /**
     * Tests deserializing the {@link com.arassec.artivact.core.model.page.widget.SpaceWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeSpaceWidget() {
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
     * Tests deserializing the {@link com.arassec.artivact.core.model.page.widget.ImageTextWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeImageTextWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(jsonParserMock, Map.class)).thenReturn(Map.of(
                "type", WidgetType.IMAGE_TEXT.toString()
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();
        Widget widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertInstanceOf(ImageTextWidget.class, widget);
    }

    /**
     * Tests deserializing the {@link ImageGalleryWidget}.
     */
    @Test
    @SneakyThrows
    void testDeserializeImageGalleryWidget() {
        JsonParser jsonParserMock = mock(JsonParser.class);
        DeserializationContext deserializationContextMock = mock(DeserializationContext.class);

        when(deserializationContextMock.readValue(jsonParserMock, Map.class)).thenReturn(Map.of(
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

        when(deserializationContextMock.readValue(jsonParserMock, Map.class)).thenReturn(Map.of(
                "type", "UNKNOWN_WIDGET",
                "invalid", true
        ));

        WidgetDeserializer widgetDeserializer = new WidgetDeserializer();

        var widget = widgetDeserializer.deserialize(jsonParserMock, deserializationContextMock);

        assertNull(widget);
    }

}
