package com.arassec.artivact.adapter.out.database.springdata.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

/**
 * Page entity.
 */
@Data
@Entity
@Table(name = "av_page")
public class PageEntity {

    /**
     * The page ID.
     */
    @Id
    private String id;

    /**
     * Technical version.
     */
    @Version
    private Integer version;

    /**
     * {@code true} if this is the application's index page, {@code false} otherwise.
     */
    private boolean indexPage;

    /**
     * The page content as JSON.
     */
    private String contentJson;

    /**
     * An alias under which this page can be opened, instead of using its ID.
     */
    private String alias;

}
