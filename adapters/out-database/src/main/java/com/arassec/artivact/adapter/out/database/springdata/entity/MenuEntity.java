package com.arassec.artivact.adapter.out.database.springdata.entity;

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
     * The menu's ID.
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
     * The menu's data as JSON.
     */
    private String contentJson;

}
