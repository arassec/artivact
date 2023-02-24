package com.arassec.artivact.creator.standalone.core.adapter.model.creator;

import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class FallbackModelCreatorAdapter implements ModelCreatorAdapter {

    @Override
    public String getDefaultPipeline() {
        return "fallback-default";
    }

    @Override
    public List<String> getPipelines() {
        return List.of("fallback-default", "fallback-extended");
    }

    @Override
    public void createModel(CreatorArtivact creatorArtivact, String pipeline, ProgressMonitor progressMonitor) {
        log.info("Fallback model creator called for artivact: {}", creatorArtivact.getId());
    }

    @Override
    public void cancelModelCreation() {
        log.info("Fallback model creator called to cancel model creation.");
    }

    @Override
    public boolean supportsCancellation() {
        return false;
    }

}
