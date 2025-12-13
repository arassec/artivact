package com.arassec.artivact.application.port.in.favorite;

/**
 * Use case to remove an item from favorites.
 */
public interface UnmarkItemAsFavoriteUseCase {

    /**
     * Removes an item from favorites for the current user.
     *
     * @param itemId The item ID.
     */
    void unmarkAsFavorite(String itemId);

}
