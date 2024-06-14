package com.arassec.artivact.backend.service.model.configuration;

import com.arassec.artivact.backend.service.model.tag.Tag;
import lombok.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of available tags.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagsConfiguration {

    /**
     * The available tags.
     */
    @Builder.Default
    private List<Tag> tags = new LinkedList<>();

}
