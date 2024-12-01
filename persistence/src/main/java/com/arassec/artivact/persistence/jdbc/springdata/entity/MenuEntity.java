package com.arassec.artivact.persistence.jdbc.springdata.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

/**
 * Menu entity.
 */
@Data
@Entity
@Table(name = "av_menu")
public class MenuEntity {

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
     * Defines the sort order of the menu.
     */
    private Integer sortOrder;

    /**
     * The item data as JSON.
     */
    private String contentJson;

}
