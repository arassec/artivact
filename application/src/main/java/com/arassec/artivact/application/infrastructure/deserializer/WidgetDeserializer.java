package com.arassec.artivact.application.infrastructure.deserializer;

import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.widget.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Custom {@link ObjectMapper} deserializer for Artivact widgets.
 */
@Slf4j
public class WidgetDeserializer extends StdDeserializer<Widget> {

    /**
     * The object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance.
     */
    public WidgetDeserializer() {
        super(Widget.class);
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Widget deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Map<String, Object> map = deserializationContext.readValue(jsonParser, Map.class);
        try {
            Class<?> classOfType = getClassOfType(getType(map));
            if (classOfType == null) {
                return null;
            }
            Widget widget = (Widget) objectMapper.readValue(objectMapper.writeValueAsString(map), classOfType);
            if (widget.getNavigationTitle() == null) {
                widget.setNavigationTitle(TranslatableString.builder().value("").build());
            }
            if (widget instanceof ItemSearchWidget itemSearchWidget) {
                if (itemSearchWidget.getHeading() == null) {
                    itemSearchWidget.setHeading(TranslatableString.builder().value("").build());
                }
                if (itemSearchWidget.getContent() == null) {
                    itemSearchWidget.setContent(TranslatableString.builder().value("").build());
                }
            }
            return widget;
        } catch (NoSuchElementException e) {
            log.warn("No widget found for type {}. Ignoring widget...", getType(map));
            return null;
        }
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

    /**
     * Returns a widget's class for its type.
     *
     * @param type The widget's type.
     * @return The class of the widgte.
     */
    private Class<? extends Widget> getClassOfType(String type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case "PAGE_TITLE" -> {
                return PageTitleWidget.class;
            }
            case "TEXT" -> {
                return TextWidget.class;
            }
            case "ITEM_SEARCH" -> {
                return ItemSearchWidget.class;
            }
            case "INFO_BOX" -> {
                return InfoBoxWidget.class;
            }
            case "AVATAR" -> {
                return AvatarWidget.class;
            }
            case "IMAGE_GALLERY" -> {
                return ImageGalleryWidget.class;
            }
            case "BUTTONS" -> {
                return ButtonsWidget.class;
            }
            default -> {
                return null;
            }
        }
    }

}
