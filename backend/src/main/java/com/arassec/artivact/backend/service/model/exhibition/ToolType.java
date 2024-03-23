package com.arassec.artivact.backend.service.model.exhibition;

import com.arassec.artivact.backend.service.model.exhibition.tool.ItemsTool;
import com.arassec.artivact.backend.service.model.exhibition.tool.TitleTool;
import lombok.Getter;

import java.util.Arrays;

/**
 * Type for tools.
 */
@Getter
public enum ToolType {

    /**
     * A title element.
     */
    TITLE(TitleTool.class),

    /**
     * A list of items to present.
     */
    ITEMS(ItemsTool.class);

    /**
     * This tool's class.
     */
    private final Class<? extends Tool> toolClass;

    /**
     * Creates a new instance.
     *
     * @param toolClass This tool's class.
     */
    ToolType(Class<? extends Tool> toolClass) {
        this.toolClass = toolClass;
    }

    /**
     * Returns the java class for the given type.
     *
     * @param type The type to get the java class for.
     * @return The class.
     */
    public static Class<?> getClassOfType(String type) {
        return Arrays.stream(values())
                .filter(value -> value.name().equals(type))
                .findFirst()
                .orElseThrow()
                .getToolClass();
    }

}
