package com.arassec.artivact.application.port.in.item;

/**
 * Use case for deleting an item.
 */
public interface DeleteItemUseCase {

    /**
     * Deletes an item.
     *
     * @param itemId The ID of the item to delete.
     */
    void delete(String itemId);

}
