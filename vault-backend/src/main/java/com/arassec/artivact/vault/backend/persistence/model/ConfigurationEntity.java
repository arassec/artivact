package com.arassec.artivact.vault.backend.persistence.model;

import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Data
@Entity
@Table(name = "av_configuration")
public class ConfigurationEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    private String content;

}
