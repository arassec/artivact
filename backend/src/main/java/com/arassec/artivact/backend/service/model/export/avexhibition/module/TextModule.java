package com.arassec.artivact.backend.service.model.export.avexhibition.module;

import com.arassec.artivact.backend.service.model.export.avexhibition.ArtivactExhibitionModule;
import com.arassec.artivact.backend.service.model.export.avexhibition.ModuleType;
import com.arassec.artivact.backend.service.model.export.avexhibition.TranslatableText;
import lombok.Getter;

@Getter
public class TextModule extends ArtivactExhibitionModule {

    private final TranslatableText heading;

    private final TranslatableText content;

    public TextModule(String id, TranslatableText heading, TranslatableText content) {
        super(id, ModuleType.TEXT);
        this.heading = heading;
        this.content = content;
    }

}
