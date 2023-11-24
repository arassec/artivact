package com.arassec.artivact.backend.service.model.export.avexhibition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemData {

    private String id;

    private TranslatableText title;

    private TranslatableText description;

    private String modelFile;

}
