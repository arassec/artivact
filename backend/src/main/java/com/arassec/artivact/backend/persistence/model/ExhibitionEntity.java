package com.arassec.artivact.backend.persistence.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

/**
 * Exhibition entity.
 */
@Data
@Entity
@Table(name = "av_exhibition")
public class ExhibitionEntity {

    /**
     * The exhibition ID.
     */
    @Id
    private String id;

    /**
     * Technical version.
     */
    @Version
    private Integer version;

    /**
     * The exhibition data as JSON.
     */
    private String contentJson;

}
