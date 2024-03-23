package com.arassec.artivact.backend.service.model.exhibition;

import com.arassec.artivact.backend.service.model.TranslatableString;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * A topic maps to a web-page and capsules a special topic of an exhibition.
 */
@Data
public class Topic {

    /**
     * The topic's title.
     */
    private TranslatableString title;

    /**
     * The topic's tools.
     */
    private List<Tool> tools = new LinkedList<>();

}
