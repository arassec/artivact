package com.arassec.artivact.application.service.favorite;

import com.arassec.artivact.application.port.in.favorite.IsItemFavoriteUseCase;
import com.arassec.artivact.application.port.in.favorite.ListFavoriteItemsUseCase;
import com.arassec.artivact.application.port.in.favorite.MarkItemAsFavoriteUseCase;
import com.arassec.artivact.application.port.in.favorite.UnmarkItemAsFavoriteUseCase;
import com.arassec.artivact.application.port.out.repository.FavoriteRepository;
import com.arassec.artivact.domain.model.favorite.Favorite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing favorites.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ManageFavoriteService implements MarkItemAsFavoriteUseCase,
        UnmarkItemAsFavoriteUseCase,
        IsItemFavoriteUseCase,
        ListFavoriteItemsUseCase {

    private final FavoriteRepository favoriteRepository;

    /**
     * Marks an item as favorite for the currently logged-in user.
     *
     * @param itemId The ID of the item to mark as favorite.
     */
    @Override
    public void markAsFavorite(String itemId) {
        String username = getCurrentUsername();
        if (username == null) {
            log.warn("Attempted to mark item {} as favorite without authentication", itemId);
            return;
        }

        if (!favoriteRepository.isFavorite(username, itemId)) {
            Favorite favorite = new Favorite(username, itemId, LocalDateTime.now());
            favoriteRepository.save(favorite);
            log.debug("Marked item {} as favorite for user {}", itemId, username);
        }
    }

    /**
     * Removes an item from the favorites of the currently logged-in user.
     *
     * @param itemId The ID of the item to unmark.
     */
    @Override
    public void unmarkAsFavorite(String itemId) {
        String username = getCurrentUsername();
        if (username == null) {
            log.warn("Attempted to unmark item {} as favorite without authentication", itemId);
            return;
        }

        favoriteRepository.delete(username, itemId);
        log.debug("Unmarked item {} as favorite for user {}", itemId, username);
    }

    /**
     * Checks if an item is marked as favorite by the currently logged-in user.
     *
     * @param itemId The ID of the item to check.
     * @return {@code true} if the item is a favorite, {@code false} otherwise.
     */
    @Override
    public boolean isFavorite(String itemId) {
        String username = getCurrentUsername();
        if (username == null) {
            return false;
        }

        return favoriteRepository.isFavorite(username, itemId);
    }

    /**
     * Lists all favorite items for the currently logged-in user.
     *
     * @return A list of favorites.
     */
    @Override
    public List<Favorite> listFavorites() {
        String username = getCurrentUsername();
        if (username == null) {
            return List.of();
        }

        return favoriteRepository.findByUsername(username);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        return null;
    }

}
