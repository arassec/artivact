package com.arassec.artivact.backend.api.model;

import com.arassec.artivact.backend.service.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * Summarizes an exhibition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionSummary {

    /**
     * The exhibition's ID.
     */
    private String exhibitionId;

    /**
     * The exhibition's title.
     */
    private TranslatableString title;

    /**
     * The exhibition's description.
     */
    private TranslatableString description;

    /**
     * IDs of menus from the application that are used to create the exhibition.
     */
    @Builder.Default
    private List<String> menuIds = new LinkedList<>();

}
