package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.FavoriteEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.FavoriteEntityRepository;
import com.arassec.artivact.domain.model.favorite.Favorite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JdbcFavoriteRepository}.
 * <p>
 * Tests the repository implementation using Mockito to mock the underlying Spring Data repository.
 * Focuses on verifying correct mapping between domain objects and entities, as well as proper
 * delegation to the Spring Data repository.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JdbcFavoriteRepository Unit Tests")
class JdbcFavoriteRepositoryTest {

    /**
     * Mocked Spring Data repository for database access.
     */
    @Mock
    private FavoriteEntityRepository favoriteEntityRepository;

    /**
     * The repository under test with mocked dependencies injected.
     */
    @InjectMocks
    private JdbcFavoriteRepository favoriteRepository;

    private static final String USERNAME_1 = "user1";
    private static final String USERNAME_2 = "user2";
    private static final String ITEM_ID_1 = "item-1";
    private static final String ITEM_ID_2 = "item-2";

    /**
     * Tests that saving a favorite correctly maps the domain object to an entity
     * and delegates the save operation to the Spring Data repository.
     */
    @Test
    @DisplayName("Should save favorite successfully")
    void shouldSaveFavorite() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();
        Favorite favorite = new Favorite(USERNAME_1, ITEM_ID_1, createdAt);

        // When
        favoriteRepository.save(favorite);

        // Then
        ArgumentCaptor<FavoriteEntity> entityCaptor = ArgumentCaptor.forClass(FavoriteEntity.class);
        verify(favoriteEntityRepository, times(1)).save(entityCaptor.capture());

        FavoriteEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getUsername()).isEqualTo(USERNAME_1);
        assertThat(capturedEntity.getItemId()).isEqualTo(ITEM_ID_1);
        assertThat(capturedEntity.getCreatedAt()).isEqualTo(createdAt);
    }

    /**
     * Tests that deleting a favorite by username and itemId correctly constructs
     * the composite key and delegates the delete operation to the Spring Data repository.
     */
    @Test
    @DisplayName("Should delete favorite by username and itemId")
    void shouldDeleteFavorite() {
        // Given
        // No setup needed

        // When
        favoriteRepository.delete(USERNAME_1, ITEM_ID_1);

        // Then
        ArgumentCaptor<FavoriteEntity.FavoriteId> idCaptor = ArgumentCaptor.forClass(FavoriteEntity.FavoriteId.class);
        verify(favoriteEntityRepository, times(1)).deleteById(idCaptor.capture());

        FavoriteEntity.FavoriteId capturedId = idCaptor.getValue();
        assertThat(capturedId.getUsername()).isEqualTo(USERNAME_1);
        assertThat(capturedId.getItemId()).isEqualTo(ITEM_ID_1);
    }

    /**
     * Tests that attempting to delete a non-existing favorite still calls the repository
     * without throwing an exception (idempotent operation).
     */
    @Test
    @DisplayName("Should call repository when deleting non-existing favorite")
    void shouldCallRepositoryWhenDeletingNonExistingFavorite() {
        // Given
        String nonExistingUsername = "nonexistent";
        String nonExistingItemId = "nonexistent-item";

        // When
        favoriteRepository.delete(nonExistingUsername, nonExistingItemId);

        // Then
        verify(favoriteEntityRepository, times(1)).deleteById(any(FavoriteEntity.FavoriteId.class));
    }

    /**
     * Tests that checking if an item is marked as favorite returns true
     * when the favorite exists in the database.
     */
    @Test
    @DisplayName("Should return true when favorite exists")
    void shouldReturnTrueWhenFavoriteExists() {
        // Given
        when(favoriteEntityRepository.existsByUsernameAndItemId(USERNAME_1, ITEM_ID_1)).thenReturn(true);

        // When
        boolean isFavorite = favoriteRepository.isFavorite(USERNAME_1, ITEM_ID_1);

        // Then
        assertThat(isFavorite).isTrue();
        verify(favoriteEntityRepository, times(1)).existsByUsernameAndItemId(USERNAME_1, ITEM_ID_1);
    }

    /**
     * Tests that checking if an item is marked as favorite returns false
     * when the favorite does not exist in the database.
     */
    @Test
    @DisplayName("Should return false when favorite does not exist")
    void shouldReturnFalseWhenFavoriteDoesNotExist() {
        // Given
        when(favoriteEntityRepository.existsByUsernameAndItemId(USERNAME_1, ITEM_ID_1)).thenReturn(false);

        // When
        boolean isFavorite = favoriteRepository.isFavorite(USERNAME_1, ITEM_ID_1);

        // Then
        assertThat(isFavorite).isFalse();
        verify(favoriteEntityRepository, times(1)).existsByUsernameAndItemId(USERNAME_1, ITEM_ID_1);
    }

    /**
     * Tests that finding all favorites for a user returns them in ascending order
     * by creation date and correctly maps entities to domain objects.
     */
    @Test
    @DisplayName("Should find all favorites by username ordered by creation date")
    void shouldFindAllFavoritesByUsernameOrderedByCreatedAt() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusSeconds(3600);
        LocalDateTime later = now.plusSeconds(3600);

        FavoriteEntity entity1 = createFavoriteEntity(ITEM_ID_2, earlier);
        FavoriteEntity entity2 = createFavoriteEntity(ITEM_ID_1, now);
        FavoriteEntity entity3 = createFavoriteEntity("item-3", later);

        when(favoriteEntityRepository.findByUsernameOrderByCreatedAtAsc(USERNAME_1))
                .thenReturn(List.of(entity1, entity2, entity3));

        // When
        List<Favorite> favorites = favoriteRepository.findByUsername(USERNAME_1);

        // Then
        assertThat(favorites).hasSize(3);
        assertThat(favorites.get(0).getItemId()).isEqualTo(ITEM_ID_2);
        assertThat(favorites.get(0).getCreatedAt()).isEqualTo(earlier);
        assertThat(favorites.get(1).getItemId()).isEqualTo(ITEM_ID_1);
        assertThat(favorites.get(1).getCreatedAt()).isEqualTo(now);
        assertThat(favorites.get(2).getItemId()).isEqualTo("item-3");
        assertThat(favorites.get(2).getCreatedAt()).isEqualTo(later);

        assertThat(favorites).allMatch(f -> f.getUsername().equals(USERNAME_1));
        verify(favoriteEntityRepository, times(1)).findByUsernameOrderByCreatedAtAsc(USERNAME_1);
    }

    /**
     * Tests that finding favorites for a user with no favorites returns an empty list.
     */
    @Test
    @DisplayName("Should return empty list when user has no favorites")
    void shouldReturnEmptyListWhenUserHasNoFavorites() {
        // Given
        when(favoriteEntityRepository.findByUsernameOrderByCreatedAtAsc(USERNAME_2))
                .thenReturn(List.of());

        // When
        List<Favorite> favorites = favoriteRepository.findByUsername(USERNAME_2);

        // Then
        assertThat(favorites).isEmpty();
        verify(favoriteEntityRepository, times(1)).findByUsernameOrderByCreatedAtAsc(USERNAME_2);
    }

    /**
     * Tests that deleting all favorites for a specific item correctly delegates
     * the delete operation to the Spring Data repository.
     */
    @Test
    @DisplayName("Should delete all favorites for specific item")
    void shouldDeleteAllFavoritesForItem() {
        // Given
        doNothing().when(favoriteEntityRepository).deleteByItemId(ITEM_ID_1);

        // When
        favoriteRepository.deleteByItemId(ITEM_ID_1);

        // Then
        verify(favoriteEntityRepository, times(1)).deleteByItemId(ITEM_ID_1);
    }

    /**
     * Tests that attempting to delete favorites for a non-existing item still calls
     * the repository without throwing an exception (idempotent operation).
     */
    @Test
    @DisplayName("Should call repository when deleting favorites for non-existing item")
    void shouldCallRepositoryWhenDeletingFavoritesForNonExistingItem() {
        // Given
        String nonExistingItemId = "nonexistent-item";
        doNothing().when(favoriteEntityRepository).deleteByItemId(nonExistingItemId);

        // When
        favoriteRepository.deleteByItemId(nonExistingItemId);

        // Then
        verify(favoriteEntityRepository, times(1)).deleteByItemId(nonExistingItemId);
    }

    /**
     * Tests that deleting all favorites for a specific user correctly delegates
     * the delete operation to the Spring Data repository.
     */
    @Test
    @DisplayName("Should delete all favorites for specific user")
    void shouldDeleteAllFavoritesForUser() {
        // Given
        doNothing().when(favoriteEntityRepository).deleteByUsername(USERNAME_1);

        // When
        favoriteRepository.deleteByUsername(USERNAME_1);

        // Then
        verify(favoriteEntityRepository, times(1)).deleteByUsername(USERNAME_1);
    }

    /**
     * Tests that attempting to delete favorites for a non-existing user still calls
     * the repository without throwing an exception (idempotent operation).
     */
    @Test
    @DisplayName("Should call repository when deleting favorites for non-existing user")
    void shouldCallRepositoryWhenDeletingFavoritesForNonExistingUser() {
        // Given
        String nonExistingUsername = "nonexistent";
        doNothing().when(favoriteEntityRepository).deleteByUsername(nonExistingUsername);

        // When
        favoriteRepository.deleteByUsername(nonExistingUsername);

        // Then
        verify(favoriteEntityRepository, times(1)).deleteByUsername(nonExistingUsername);
    }

    /**
     * Tests that entities are correctly mapped to domain objects with all properties
     * transferred correctly (username, itemId, createdAt).
     */
    @Test
    @DisplayName("Should map entity to domain model correctly")
    void shouldMapEntityToDomainModelCorrectly() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();
        FavoriteEntity entity = createFavoriteEntity(ITEM_ID_1, createdAt);

        when(favoriteEntityRepository.findByUsernameOrderByCreatedAtAsc(USERNAME_1))
                .thenReturn(List.of(entity));

        // When
        List<Favorite> favorites = favoriteRepository.findByUsername(USERNAME_1);

        // Then
        assertThat(favorites).hasSize(1);
        Favorite favorite = favorites.getFirst();
        assertThat(favorite.getUsername()).isEqualTo(USERNAME_1);
        assertThat(favorite.getItemId()).isEqualTo(ITEM_ID_1);
        assertThat(favorite.getCreatedAt()).isEqualTo(createdAt);
    }

    /**
     * Creates a test favorite entity with the given itemId and creation timestamp.
     * The username is set to {@link #USERNAME_1}.
     *
     * @param itemId    the item ID for the favorite
     * @param createdAt the creation timestamp
     * @return a configured FavoriteEntity for testing
     */
    private FavoriteEntity createFavoriteEntity(String itemId, LocalDateTime createdAt) {
        FavoriteEntity entity = new FavoriteEntity();
        entity.setUsername(JdbcFavoriteRepositoryTest.USERNAME_1);
        entity.setItemId(itemId);
        entity.setCreatedAt(createdAt);
        return entity;
    }
}
