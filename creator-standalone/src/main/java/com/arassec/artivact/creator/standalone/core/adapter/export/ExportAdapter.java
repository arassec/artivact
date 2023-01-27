package com.arassec.artivact.creator.standalone.core.adapter.export;

import com.arassec.artivact.creator.standalone.core.model.Artivact;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;

import java.nio.file.Path;

public interface ExportAdapter {

    String getId();

    void export(Artivact artivact, Path targetDir, ProgressMonitor progressMonitor);

}
