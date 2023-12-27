package com.arassec.artivact.backend.service.model.exhibition;

import lombok.Getter;

@Getter
public abstract class Tool {

    private final ToolType type;

    protected Tool(ToolType type) {
        this.type = type;
    }

}
