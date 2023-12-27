package com.arassec.artivact.backend.api.model;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.tag.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetails {

    private String id;

    private Integer version;

    private List<String> restrictions;

    private TranslatableString title;

    private TranslatableString description;

    private List<Asset> images;

    private List<Asset> models;

    private List<ImageSet> creationImageSets;

    private List<ModelSet> creationModelSets;

    private Map<String, String> properties;

    private List<Tag> tags;

}
