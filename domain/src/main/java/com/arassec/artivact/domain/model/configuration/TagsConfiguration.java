package com.arassec.artivact.domain.model.configuration;

import com.arassec.artivact.domain.model.tag.Tag;
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
