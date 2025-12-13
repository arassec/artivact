package com.arassec.artivact.application.port.out.repository;

import com.arassec.artivact.domain.model.favorite.Favorite;

import java.util.List;

/**
 * Repository for favorites.
 */
public interface FavoriteRepository {

    /**
     * Adds a favorite for the given user and item.
     *
     * @param favorite The favorite to add.
     */
    void save(Favorite favorite);

    /**
     * Removes a favorite for the given user and item.
     *
     * @param username The username.
     * @param itemId   The item ID.
     */
    void delete(String username, String itemId);

    /**
     * Checks if an item is marked as favorite by a user.
     *
     * @param username The username.
     * @param itemId   The item ID.
     * @return True if the item is a favorite, false otherwise.
     */
    boolean isFavorite(String username, String itemId);

    /**
     * Returns all favorites for a given user.
     *
     * @param username The username.
     * @return List of favorites.
     */
    List<Favorite> findByUsername(String username);

    /**
     * Deletes all favorites for a given item.
     *
     * @param itemId The item ID.
     */
    void deleteByItemId(String itemId);

    /**
     * Deletes all favorites for a given user.
     *
     * @param username The username.
     */
    void deleteByUsername(String username);

}
