package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.item.Item;

/**
 * Use case for save item operations.
 */
public interface SaveItemUseCase {

    /**
     * Saves an item.
     *
     * @param item The item to save.
     * @return The updated item.
     */
    Item save(Item item);

}
