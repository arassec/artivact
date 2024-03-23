package com.arassec.artivact.backend.persistence.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

/**
 * Item entity.
 */
@Data
@Entity
@Table(name = "av_item")
public class ItemEntity {

    /**
     * The item's ID.
     */
    @Id
    private String id;

    /**
     * Technical version.
     */
    @Version
    private Integer version;

    /**
     * The item data as JSON.
     */
    private String contentJson;

}
