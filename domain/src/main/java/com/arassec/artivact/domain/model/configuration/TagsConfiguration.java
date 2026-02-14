package com.arassec.artivact.domain.model.configuration;

import com.arassec.artivact.domain.model.tag.Tag;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of available tags.
 */
@Getter
@Setter
@NoArgsConstructor
@Builder
public class TagsConfiguration {

    /**
     * The available tags.
     */
    @Builder.Default
    private List<Tag> tags = new LinkedList<>();

    /**
     * Creates a new tags configuration with the specified tags.
     *
     * @param tags The available tags.
     */
    public TagsConfiguration(List<Tag> tags) {
        if (tags != null) {
            this.tags = tags;
        }
    }

}
