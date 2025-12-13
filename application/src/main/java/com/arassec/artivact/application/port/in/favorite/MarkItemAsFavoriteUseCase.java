package com.arassec.artivact.application.port.in.favorite;

/**
 * Use case to mark an item as favorite.
 */
public interface MarkItemAsFavoriteUseCase {

    /**
     * Marks an item as favorite for the current user.
     *
     * @param itemId The item ID.
     */
    void markAsFavorite(String itemId);

}
