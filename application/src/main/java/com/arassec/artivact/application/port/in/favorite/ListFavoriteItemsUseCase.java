package com.arassec.artivact.application.port.in.favorite;

import com.arassec.artivact.domain.model.favorite.Favorite;

import java.util.List;

/**
 * Use case to list favorite items.
 */
public interface ListFavoriteItemsUseCase {

    /**
     * Returns all favorite items for the current user.
     *
     * @return List of favorites.
     */
    List<Favorite> listFavorites();

}
