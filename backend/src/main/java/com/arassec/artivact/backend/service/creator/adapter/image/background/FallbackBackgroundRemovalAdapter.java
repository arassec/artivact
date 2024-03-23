package com.arassec.artivact.backend.service.creator.adapter.image.background;


import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Fallback adapter for background removal that can be used if no background removal is required.
 */
@Slf4j
@Getter
@Component
public class FallbackBackgroundRemovalAdapter extends BaseBackgroundRemovalAdapter {

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.FALLBACK_BACKGROUND_REMOVAL_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeBackground(Path filePath) {
        log.info("Fallback background removal called for file: {} with target directory: {}", filePath, initParams.getTargetDir());
        log.info("Background is not removed due to fallback adapter!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeBackgrounds(List<Path> filePaths) {
        log.info("Fallback background removal adapter called for multiple images.");
        filePaths.forEach(filePath -> log.info("Fallback background removal adapter called for image: {}", filePath));
    }

}
