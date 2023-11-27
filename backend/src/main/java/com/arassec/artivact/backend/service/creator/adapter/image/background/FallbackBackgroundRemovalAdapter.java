package com.arassec.artivact.backend.service.creator.adapter.image.background;


import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.model.item.asset.ImageSet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Fallback adapter for background removal that can be used if no background removal is required.
 */
@Slf4j
@Getter
@Component
public class FallbackBackgroundRemovalAdapter extends BaseBackgroundRemovalAdapter {

    /**
     * The implementation supported by this adapter.
     */
    private final AdapterImplementation supportedImplementation = AdapterImplementation.FALLBACK_BACKGROUND_REMOVAL_ADAPTER;

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
    public void removeBackgrounds(ImageSet imageSet) {
        log.info("Fallback background removal adapter called for image-set: {}", imageSet);
        imageSet.getImages().forEach(image -> log.info("Fallback background removal adapter called for image: {}", image));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration(AdapterConfiguration adapterConfiguration) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean healthy(AdapterConfiguration adapterConfiguration) {
        return true;
    }

}
