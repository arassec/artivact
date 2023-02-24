package com.arassec.artivact.creator.standalone.core.adapter.image.background;

import com.arassec.artivact.common.util.FileUtil;
import com.arassec.artivact.creator.standalone.core.adapter.BaseAdapter;
import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;

import java.nio.file.Path;

public abstract class BaseRemBgBackgroundRemovalAdapter extends BaseAdapter implements BackgroundRemovalAdapter {

    protected Path tempDir;

    protected Path remBgInputDir;

    protected Path remBgOutputDir;

    protected void initializeEnvironment(CreatorArtivact creatorArtivact, ArtivactImageSet imageSet, ProgressMonitor progressMonitor, FileUtil fileUtil) {
        tempDir = creatorArtivact.getProjectTempDir();

        remBgInputDir = tempDir.resolve("rembg-input");
        remBgOutputDir = tempDir.resolve("rembg-output");

        fileUtil.emptyDir(tempDir);
        fileUtil.createDirIfRequired(remBgInputDir);
        fileUtil.createDirIfRequired(remBgOutputDir);

        copyImages(creatorArtivact, imageSet, remBgInputDir, progressMonitor);
    }

}
