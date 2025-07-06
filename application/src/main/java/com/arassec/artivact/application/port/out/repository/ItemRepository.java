package com.arassec.artivact.application.port.out.repository;

import com.arassec.artivact.domain.model.item.Item;

import java.util.List;
import java.util.Optional;

/**
 * Repository for items.
 */
public interface ItemRepository {

    /**
     * Creates a new item or updates an existing item.
     *
     * @param item The item to save.
     * @return The updated/saved item.
     */
    Item save(Item item);

    /**
     * Deletes the item with the given ID.
     *
     * @param itemId The item's ID.
     */
    void deleteById(String itemId);

    /**
     * Returns the item with the given ID.
     *
     * @param itemId The item's ID.
     * @return The item with the given ID.
     */
    Optional<Item> findById(String itemId);

    /**
     * Returns all item IDs of items which have been modified locally and not yet been synced with a remote instance.
     *
     * @param maxItems Maximum number of item IDs to find.
     * @return List of item IDs.
     */
    List<String> findItemIdsForRemoteExport(int maxItems);

    /**
     * Returns all items.
     *
     * @return List of all items.
     */
    List<Item> findAll();

    /**
     * Returns all items up to {@code maxResults}.
     *
     * @param maxResults The maximum number of items to return.
     * @return List of items.
     */
    List<Item> findAll(int maxResults);

    /**
     * Returns all items which have an ID from the supplied list of IDs.
     *
     * @param itemIds The item IDs to load items for.
     * @return List of items.
     */
    List<Item> findAllById(List<String> itemIds);

}
