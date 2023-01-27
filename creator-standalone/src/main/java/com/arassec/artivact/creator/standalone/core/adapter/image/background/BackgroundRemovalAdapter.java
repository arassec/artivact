package com.arassec.artivact.creator.standalone.core.adapter.image.background;

import com.arassec.artivact.creator.standalone.core.model.Artivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;

import java.nio.file.Path;
import java.util.List;

public interface BackgroundRemovalAdapter {

    List<Path> removeBackgroundFromImages(Artivact artivact, ArtivactImageSet imageSet,
                                          ProgressMonitor progressMonitor);

}
