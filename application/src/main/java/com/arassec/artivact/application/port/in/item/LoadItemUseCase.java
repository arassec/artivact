package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.item.Item;

import java.util.List;
import java.util.Optional;

public interface LoadItemUseCase {

    /**
     * Loads an item without regard to restrictions but translations.
     *
     * @param itemId The item's ID.
     * @return The item.
     */
    Optional<Item> load(String itemId);

    /**
     * Loads an item without regard to restrictions but translations.
     *
     * @param itemId The item's ID.
     * @return The item.
     */
    Item loadTranslated(String itemId);

    /**
     * Loads the item with the given ID.
     *
     * @param itemId The item's ID.
     * @return The item.
     */
    Item loadTranslatedRestricted(String itemId);


    /**
     * Loads all items that have been modified since last uploaded to a remote Artivact instance.
     *
     * @return List of modified items.
     */
    List<Item> loadModified(int maxItems);

}
