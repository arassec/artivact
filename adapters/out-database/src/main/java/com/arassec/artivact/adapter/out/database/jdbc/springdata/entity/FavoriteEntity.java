package com.arassec.artivact.adapter.out.database.jdbc.springdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Favorite entity for database persistence.
 */
@Data
@Entity
@Table(name = "av_favorite")
@IdClass(FavoriteEntity.FavoriteId.class)
public class FavoriteEntity {

    /**
     * The username of the user who favorited the item.
     */
    @Id
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    /**
     * The ID of the favorited item.
     */
    @Id
    @Column(name = "item_id", nullable = false, length = 128)
    private String itemId;

    /**
     * The timestamp when the favorite was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Composite key for FavoriteEntity.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteId implements Serializable {
        private String username;
        private String itemId;
    }

}
