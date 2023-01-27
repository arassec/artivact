package com.arassec.artivact.creator.standalone.core.adapter.model.creator;

import com.arassec.artivact.creator.standalone.core.model.Artivact;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;

import java.util.List;

public interface ModelCreatorAdapter {

    String getDefaultPipeline();

    List<String> getPipelines();

    void createModel(Artivact artivact, String pipeline, ProgressMonitor progressMonitor);

    void cancelModelCreation();

}
