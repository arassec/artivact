package com.arassec.artivact.creator.standalone.core.adapter.model.editor;

import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactAsset;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FallbackModelEditorAdapter implements ModelEditorAdapter {

    @Override
    public void openModel(CreatorArtivact creatorArtivact, ArtivactAsset asset) {
        log.info("Fallback model editor called for artivact: {}", creatorArtivact.getId());
    }

}
