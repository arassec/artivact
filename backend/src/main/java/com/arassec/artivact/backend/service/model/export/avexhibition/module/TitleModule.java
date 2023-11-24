package com.arassec.artivact.backend.service.model.export.avexhibition.module;

import com.arassec.artivact.backend.service.model.export.avexhibition.ArtivactExhibitionModule;
import com.arassec.artivact.backend.service.model.export.avexhibition.ModuleType;
import com.arassec.artivact.backend.service.model.export.avexhibition.TranslatableText;
import lombok.Getter;

@Getter
public class TitleModule extends ArtivactExhibitionModule {

    private final TranslatableText title;

    private final String image;

    public TitleModule(String id, TranslatableText title, String image) {
        super(id, ModuleType.TITLE);
        this.title = title;
        this.image = image;
    }

}
