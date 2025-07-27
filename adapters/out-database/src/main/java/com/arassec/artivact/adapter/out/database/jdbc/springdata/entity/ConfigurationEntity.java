package com.arassec.artivact.adapter.out.database.jdbc.springdata.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

/**
 * Application configuration entity.
 */
@Data
@Entity
@Table(name = "av_configuration")
public class ConfigurationEntity {

    /**
     * The configuration ID.
     */
    @Id
    private String id;

    /**
     * Technical version.
     */
    @Version
    private Integer version;

    /**
     * The configuration as JSON.
     */
    private String contentJson;

}
