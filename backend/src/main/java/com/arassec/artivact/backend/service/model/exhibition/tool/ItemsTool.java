package com.arassec.artivact.backend.service.model.exhibition.tool;

import com.arassec.artivact.backend.service.model.exhibition.Tool;
import com.arassec.artivact.backend.service.model.exhibition.ToolType;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ItemsTool extends Tool {

    private List<String> itemIds = new LinkedList<>();

    public ItemsTool() {
        super(ToolType.ITEMS);
    }
}
