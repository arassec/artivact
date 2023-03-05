package com.arassec.artivact.vault.backend.web.model;

import com.arassec.artivact.vault.backend.service.model.TranslatedItem;
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
public class ArtivactDetails {

    private String id;

    private Integer version;

    private TranslatedItem title;

    private TranslatedItem description;

    private List<MediaEntry> images;

    private List<MediaEntry> models;

    private Map<String, String> properties;

    private List<TranslatedTag> tags;

}
