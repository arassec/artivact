package com.arassec.artivact.creator.standalone.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtivactModel extends ArtivactAsset {

    private String comment;

    @Builder.Default
    private List<String> exportFiles = new LinkedList<>();

    @Override
    public AssetType getType() {
        return AssetType.MODEL;
    }

}
