package com.arassec.artivact.creator.core.adapter.image.background;

import com.arassec.artivact.creator.core.adapter.BaseAdapter;
import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.core.util.FileHelper;
import com.arassec.artivact.creator.core.util.ProgressMonitor;

import java.nio.file.Path;

public abstract class BaseRemBgBackgroundRemovalAdapter extends BaseAdapter implements BackgroundRemovalAdapter {

    protected Path tempDir;

    protected Path remBgInputDir;

    protected Path remBgOutputDir;

    protected void initializeEnvironment(Artivact artivact, ArtivactImageSet imageSet, ProgressMonitor progressMonitor, FileHelper fileHelper) {
        tempDir = artivact.getProjectRoot().resolve(FileHelper.TEMP_DIR);

        remBgInputDir = tempDir.resolve("rembg-input");
        remBgOutputDir = tempDir.resolve("rembg-output");

        fileHelper.emptyDir(tempDir);
        fileHelper.createDirIfRequired(remBgInputDir);
        fileHelper.createDirIfRequired(remBgOutputDir);

        fileHelper.copyImages(artivact, imageSet, remBgInputDir, progressMonitor);
    }

}
