package com.arassec.artivact.application.service.favorite;

import com.arassec.artivact.application.port.out.repository.FavoriteRepository;
import com.arassec.artivact.domain.model.favorite.Favorite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageFavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ManageFavoriteService manageFavoriteService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testMarkAsFavorite() {
        // Given
        String itemId = "item-123";
        String username = "testuser";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(favoriteRepository.isFavorite(username, itemId)).thenReturn(false);

        // When
        manageFavoriteService.markAsFavorite(itemId);

        // Then
        ArgumentCaptor<Favorite> favoriteCaptor = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteRepository).save(favoriteCaptor.capture());
        Favorite savedFavorite = favoriteCaptor.getValue();
        assertThat(savedFavorite.getUsername()).isEqualTo(username);
        assertThat(savedFavorite.getItemId()).isEqualTo(itemId);
        assertThat(savedFavorite.getCreatedAt()).isNotNull();
    }

    @Test
    void testMarkAsFavoriteWhenAlreadyFavorite() {
        // Given
        String itemId = "item-123";
        String username = "testuser";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(favoriteRepository.isFavorite(username, itemId)).thenReturn(true);

        // When
        manageFavoriteService.markAsFavorite(itemId);

        // Then
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void testUnmarkAsFavorite() {
        // Given
        String itemId = "item-123";
        String username = "testuser";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);

        // When
        manageFavoriteService.unmarkAsFavorite(itemId);

        // Then
        verify(favoriteRepository).delete(username, itemId);
    }

    @Test
    void testIsFavorite() {
        // Given
        String itemId = "item-123";
        String username = "testuser";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(favoriteRepository.isFavorite(username, itemId)).thenReturn(true);

        // When
        boolean result = manageFavoriteService.isFavorite(itemId);

        // Then
        assertThat(result).isTrue();
        verify(favoriteRepository).isFavorite(username, itemId);
    }

    @Test
    void testListFavorites() {
        // Given
        String username = "testuser";
        List<Favorite> expectedFavorites = List.of(
                new Favorite(username, "item-1", null),
                new Favorite(username, "item-2", null)
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(favoriteRepository.findByUsername(username)).thenReturn(expectedFavorites);

        // When
        List<Favorite> result = manageFavoriteService.listFavorites();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedFavorites);
        verify(favoriteRepository).findByUsername(username);
    }

    @Test
    void testListFavoritesWhenNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        List<Favorite> result = manageFavoriteService.listFavorites();

        // Then
        assertThat(result).isEmpty();
        verify(favoriteRepository, never()).findByUsername(any());
    }

}
