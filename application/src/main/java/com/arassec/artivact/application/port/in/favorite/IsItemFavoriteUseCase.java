package com.arassec.artivact.application.port.in.favorite;

/**
 * Use case to check if an item is marked as favorite.
 */
public interface IsItemFavoriteUseCase {

    /**
     * Checks if an item is marked as favorite for the current user.
     *
     * @param itemId The item ID.
     * @return True if the item is a favorite, false otherwise.
     */
    boolean isFavorite(String itemId);

}
