package com.arassec.artivact.backend.service.model.export.avexhibition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class ArtivactExhibitionModule {

    private final String id;

    private final ModuleType type;

}
