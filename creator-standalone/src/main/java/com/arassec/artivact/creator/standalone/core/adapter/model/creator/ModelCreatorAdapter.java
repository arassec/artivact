package com.arassec.artivact.creator.standalone.core.adapter.model.creator;

import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;

import java.util.List;

public interface ModelCreatorAdapter {

    String getDefaultPipeline();

    List<String> getPipelines();

    void createModel(CreatorArtivact creatorArtivact, String pipeline, ProgressMonitor progressMonitor);

    void cancelModelCreation();

    boolean supportsCancellation();

}
