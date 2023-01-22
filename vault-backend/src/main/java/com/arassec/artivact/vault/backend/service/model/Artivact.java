package com.arassec.artivact.vault.backend.service.model;

import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class Artivact {

    private String id;

    private Integer version;

    private TranslatableItem title;

    @Builder.Default
    private Set<String> restrictions = new HashSet<>();

    private TranslatableItem description;

    @Builder.Default
    private Map<String, String> properties = new HashMap<>();

    private MediaContent mediaContent;

    @Builder.Default
    private List<Tag> tags = new LinkedList<>();

}
