package com.arassec.artivact.backend.persistence.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

@Data
@Entity
@Table(name = "av_exhibition")
public class ExhibitionEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    private String contentJson;

}
