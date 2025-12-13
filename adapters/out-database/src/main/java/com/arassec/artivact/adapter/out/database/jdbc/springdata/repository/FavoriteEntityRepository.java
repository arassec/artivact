package com.arassec.artivact.adapter.out.database.jdbc.springdata.repository;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.FavoriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring-Data repository for {@link FavoriteEntity}s.
 */
public interface FavoriteEntityRepository extends JpaRepository<FavoriteEntity, FavoriteEntity.FavoriteId> {

    /**
     * Finds all favorites for a given username, ordered by creation date descending.
     *
     * @param username The username.
     * @return List of favorites.
     */
    List<FavoriteEntity> findByUsernameOrderByCreatedAtDesc(String username);

    /**
     * Checks if a favorite exists for a given username and item ID.
     *
     * @param username The username.
     * @param itemId   The item ID.
     * @return True if the favorite exists, false otherwise.
     */
    boolean existsByUsernameAndItemId(String username, String itemId);

    /**
     * Deletes all favorites for a given item ID.
     *
     * @param itemId The item ID.
     */
    @Modifying
    @Query("DELETE FROM FavoriteEntity f WHERE f.itemId = :itemId")
    void deleteByItemId(@Param("itemId") String itemId);

    /**
     * Deletes all favorites for a given username.
     *
     * @param username The username.
     */
    @Modifying
    @Query("DELETE FROM FavoriteEntity f WHERE f.username = :username")
    void deleteByUsername(@Param("username") String username);

}
