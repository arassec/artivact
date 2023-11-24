package com.arassec.artivact.backend.service.model.export.avexhibition.module;

import com.arassec.artivact.backend.service.model.export.avexhibition.ArtivactExhibitionModule;
import com.arassec.artivact.backend.service.model.export.avexhibition.ItemData;
import com.arassec.artivact.backend.service.model.export.avexhibition.ModuleType;
import lombok.Getter;

import java.util.List;

@Getter
public class ItemListModule extends ArtivactExhibitionModule {

    private final List<ItemData> itemData;

    public ItemListModule(String id, List<ItemData> itemData) {
        super(id, ModuleType.ITEM_LIST);
        this.itemData = itemData;
    }

}
