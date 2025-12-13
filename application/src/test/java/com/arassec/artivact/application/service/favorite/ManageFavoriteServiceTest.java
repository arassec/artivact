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

    /**
     * Tests marking an item as favorite.
     */
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

    /**
     * Tests marking an item as favorite when it is already a favorite.
     */
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

    /**
     * Tests marking an item as favorite when no user is authenticated (auth is null).
     */
    @Test
    void testMarkAsFavoriteWhenNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        manageFavoriteService.markAsFavorite("item-123");

        // Then
        verify(favoriteRepository, never()).save(any());
    }

    /**
     * Tests marking an item as favorite when the user is anonymous.
     */
    @Test
    void testMarkAsFavoriteWhenAnonymousUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");

        // When
        manageFavoriteService.markAsFavorite("item-123");

        // Then
        verify(favoriteRepository, never()).save(any());
    }

    /**
     * Tests marking an item as favorite when authentication object exists but is not authenticated.
     */
    @Test
    void testMarkAsFavoriteWhenAuthenticationIsFalse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        manageFavoriteService.markAsFavorite("item-123");

        // Then
        verify(favoriteRepository, never()).save(any());
    }

    /**
     * Tests unmarking an item as favorite.
     */
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

    /**
     * Tests unmarking an item as favorite when no user is authenticated.
     */
    @Test
    void testUnmarkAsFavoriteWhenNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        manageFavoriteService.unmarkAsFavorite("item-123");

        // Then
        verify(favoriteRepository, never()).delete(any(), any());
    }

    /**
     * Tests unmarking an item as favorite when the user is anonymous.
     */
    @Test
    void testUnmarkAsFavoriteWhenAnonymousUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");

        // When
        manageFavoriteService.unmarkAsFavorite("item-123");

        // Then
        verify(favoriteRepository, never()).delete(any(), any());
    }

    /**
     * Tests checking if an item is a favorite.
     */
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

    /**
     * Tests checking if an item is a favorite when no user is authenticated.
     */
    @Test
    void testIsFavoriteWhenNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        boolean result = manageFavoriteService.isFavorite("item-123");

        // Then
        assertThat(result).isFalse();
        verify(favoriteRepository, never()).isFavorite(any(), any());
    }

    /**
     * Tests checking if an item is a favorite when the user is anonymous.
     */
    @Test
    void testIsFavoriteWhenAnonymousUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");

        // When
        boolean result = manageFavoriteService.isFavorite("item-123");

        // Then
        assertThat(result).isFalse();
        verify(favoriteRepository, never()).isFavorite(any(), any());
    }

    /**
     * Tests listing favorite items.
     */
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
        assertThat(result).hasSize(2).isEqualTo(expectedFavorites);
        verify(favoriteRepository).findByUsername(username);
    }

    /**
     * Tests listing favorite items when no user is authenticated.
     */
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

    /**
     * Tests listing favorite items when the user is anonymous.
     */
    @Test
    void testListFavoritesWhenAnonymousUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");

        // When
        List<Favorite> result = manageFavoriteService.listFavorites();

        // Then
        assertThat(result).isEmpty();
        verify(favoriteRepository, never()).findByUsername(any());
    }

}
