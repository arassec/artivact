package com.arassec.artivact.creator.standalone.core.adapter.export;

import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;

import java.nio.file.Path;

public interface ExportAdapter {

    String getId();

    void export(CreatorArtivact creatorArtivact, Path targetDir, ProgressMonitor progressMonitor);

}
