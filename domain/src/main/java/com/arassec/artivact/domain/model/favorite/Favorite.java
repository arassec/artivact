package com.arassec.artivact.domain.model.favorite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a favorite association between a user and an item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    /**
     * The username of the user who favorited the item.
     */
    private String username;

    /**
     * The ID of the favorited item.
     */
    private String itemId;

    /**
     * The timestamp when the favorite was created.
     */
    private LocalDateTime createdAt;

}
