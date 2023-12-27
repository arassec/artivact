package com.arassec.artivact.backend.service.model.exhibition.tool;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.exhibition.Tool;
import com.arassec.artivact.backend.service.model.exhibition.ToolType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TitleTool extends Tool {

    private TranslatableString title;

    public TitleTool() {
        super(ToolType.TITLE);
    }

}
