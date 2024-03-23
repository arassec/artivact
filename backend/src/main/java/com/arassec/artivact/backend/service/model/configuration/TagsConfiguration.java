package com.arassec.artivact.backend.service.model.configuration;

import com.arassec.artivact.backend.service.model.tag.Tag;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class TagsConfiguration {

    /**
     * The available tags.
     */
    private List<Tag> tags = new LinkedList<>();

}
