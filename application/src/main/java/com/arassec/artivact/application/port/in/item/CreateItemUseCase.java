package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.item.Item;

public interface CreateItemUseCase {

    /**
     * Creates a new item.
     *
     * @return The newly created item.
     */
    Item create();

}
