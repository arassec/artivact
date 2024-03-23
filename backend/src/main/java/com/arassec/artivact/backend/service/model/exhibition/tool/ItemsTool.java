package com.arassec.artivact.backend.service.model.exhibition.tool;

import com.arassec.artivact.backend.service.model.exhibition.Tool;
import com.arassec.artivact.backend.service.model.exhibition.ToolType;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Tool to display items in an exhibition.
 */
@Getter
@Setter
public class ItemsTool extends Tool {

    /**
     * The item IDs.
     */
    private List<String> itemIds = new LinkedList<>();

    /**
     * Creates a new instance.
     */
    public ItemsTool() {
        super(ToolType.ITEMS);
    }

}
