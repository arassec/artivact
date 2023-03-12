package com.arassec.artivact.vault.backend.persistence.model;

import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "av_artivact")
public class ArtivactEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    private LocalDateTime scanned;

    private String titleJson;

    private String restrictions;

    private String descriptionJson;

    private String propertiesJson;

    private String mediaContentJson;

    private String tagsJson;

}
