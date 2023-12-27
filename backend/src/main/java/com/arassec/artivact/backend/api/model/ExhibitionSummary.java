package com.arassec.artivact.backend.api.model;

import com.arassec.artivact.backend.service.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionSummary {

    private String exhibitionId;

    private TranslatableString title;

    private TranslatableString description;

    @Builder.Default
    private List<String> menuIds = new LinkedList<>();

}
