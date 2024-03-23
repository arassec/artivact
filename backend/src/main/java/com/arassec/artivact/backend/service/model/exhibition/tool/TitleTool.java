package com.arassec.artivact.backend.service.model.exhibition.tool;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.exhibition.Tool;
import com.arassec.artivact.backend.service.model.exhibition.ToolType;
import lombok.Getter;
import lombok.Setter;

/**
 * Tool to display a title in an exhibition.
 */
@Getter
@Setter
public class TitleTool extends Tool {

    /**
     * The title.
     */
    private TranslatableString title;

    /**
     * Creates a new instance.
     */
    public TitleTool() {
        super(ToolType.TITLE);
    }

}
