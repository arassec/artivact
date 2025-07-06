package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.item.Item;

public interface SaveItemUseCase {

    /**
     * Saves an item.
     *
     * @param item The item to save.
     * @return The updated item.
     */
    Item save(Item item);

}
