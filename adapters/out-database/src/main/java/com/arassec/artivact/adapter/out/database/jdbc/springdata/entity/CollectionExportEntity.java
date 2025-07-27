package com.arassec.artivact.adapter.out.database.jdbc.springdata.entity;

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
@Table(name = "av_collection_export")
public class CollectionExportEntity {

    /**
     * The export's ID.
     */
    @Id
    private String id;

    /**
     * Technical version.
     */
    @Version
    private Integer version;

    /**
     * Defines the sort order of the export.
     */
    private Integer sortOrder;

    /**
     * The export's data as JSON.
     */
    private String contentJson;

}
