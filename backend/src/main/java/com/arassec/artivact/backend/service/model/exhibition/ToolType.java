package com.arassec.artivact.backend.service.model.exhibition;

import com.arassec.artivact.backend.service.model.exhibition.tool.ItemsTool;
import com.arassec.artivact.backend.service.model.exhibition.tool.TitleTool;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ToolType {

    TITLE(TitleTool.class),

    ITEMS(ItemsTool.class);

    private final Class<? extends Tool> toolClass;

    ToolType(Class<? extends Tool> toolClass) {
        this.toolClass = toolClass;
    }

    public static Class<?> getClassOfType(String type) {
        return Arrays.stream(values())
                .filter(value -> value.name().equals(type))
                .findFirst()
                .orElseThrow()
                .getToolClass();
    }

}
