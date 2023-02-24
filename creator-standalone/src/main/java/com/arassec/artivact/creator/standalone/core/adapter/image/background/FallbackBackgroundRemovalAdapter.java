package com.arassec.artivact.creator.standalone.core.adapter.image.background;

import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;

@Slf4j
public class FallbackBackgroundRemovalAdapter implements BackgroundRemovalAdapter {

    @Override
    public List<Path> removeBackgroundFromImages(CreatorArtivact creatorArtivact, ArtivactImageSet imageSet, ProgressMonitor progressMonitor) {
        log.info("Fallback background removal adapter called for artivact: {}", creatorArtivact.getId());
        return List.of();
    }

}
