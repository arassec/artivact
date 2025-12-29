package com.arassec.artivact.application.infrastructure.mapping;

import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.widget.*;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Custom {@link ValueDeserializer} deserializer for Artivact widgets.
 */
@Slf4j
public class WidgetDeserializer extends ValueDeserializer<Widget> {

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();

    @Override
    public Widget deserialize(JsonParser jsonParser, tools.jackson.databind.DeserializationContext deserializationContext) throws JacksonException {
        Map<String, Object> map = deserializationContext.readValue(jsonParser, new TypeReference<>() {
        });
        try {
            Class<?> classOfType = getClassOfType(getType(map));
            if (classOfType == null) {
                return null;
            }
            Widget widget = (Widget) jsonMapper.readValue(jsonMapper.writeValueAsString(map), classOfType);
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
            } else if (widget instanceof PageTitleWidget pageTitleWidget) {
                if (pageTitleWidget.getSubtitle() == null) {
                    pageTitleWidget.setSubtitle(TranslatableString.builder().value("").build());
                }
                if (pageTitleWidget.getButtonConfigs() == null) {
                    pageTitleWidget.setButtonConfigs(new LinkedList<>());
                }
            }
            return widget;
        } catch (NoSuchElementException _) {
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
