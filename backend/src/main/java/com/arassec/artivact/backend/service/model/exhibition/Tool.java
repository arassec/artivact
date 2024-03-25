package com.arassec.artivact.backend.service.model.exhibition;

import lombok.Getter;

/**
 * A tool to implement an exhibition. It maps to a web-page's widget and is the smallest exhibition building block.
 */
@Getter
public abstract class Tool {

    /**
     * Type of this tool.
     */
    private final ToolType type;

    /**
     * Creates a new instance.
     *
     * @param type The type of this tool.
     */
    protected Tool(ToolType type) {
        this.type = type;
    }

}
