package com.arassec.artivact.creator.standalone.core.service;

import com.arassec.artivact.common.util.FileUtil;
import com.arassec.artivact.creator.standalone.core.adapter.image.background.BackgroundRemovalAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.image.camera.CameraAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.image.turntable.TurntableAdapter;
import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.standalone.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final TurntableAdapter turntableAdapter;

    private final CameraAdapter cameraAdapter;

    private final BackgroundRemovalAdapter backgroundRemovalAdapter;

    private final FileUtil fileUtil;

    private final MessageSource messageSource;

    public void capturePhotos(CreatorArtivact creatorArtivact, int numPhotos, boolean useTurnTable, ProgressMonitor progressMonitor,
                              ArtivactImageSet targetImageSet) {
        Path targetDir = creatorArtivact.getProjectTempDir();

        fileUtil.emptyDir(targetDir);

        progressMonitor.setProgressPrefix(messageSource.getMessage("image-service.capture-photos.progress.prefix", null, Locale.getDefault()));

        for (var i = 0; i < numPhotos; i++) {

            if (progressMonitor.isCancelled()) {
                return;
            }

            progressMonitor.setProgress("(" + (i + 1) + "/" + numPhotos + ")");
            cameraAdapter.captureImage(targetDir, i);
            if (useTurnTable) {
                turntableAdapter.rotate(numPhotos);
            }
        }

        try (Stream<Path> imagePaths = Files.list(targetDir)) {
            List<File> images = imagePaths
                    .map(Path::toFile)
                    .toList();

            progressMonitor.setProgressPrefix(messageSource.getMessage("editor.dialog.add-images.progress.prefix", null, Locale.getDefault()));

            if (targetImageSet != null) {
                creatorArtivact.addImages(images, progressMonitor, false, useTurnTable, targetImageSet);
            } else {
                creatorArtivact.addImageSet(images, progressMonitor, false, useTurnTable);
            }
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not add captured images!", e);
        }
    }

    public List<Path> removeBackgrounds(CreatorArtivact creatorArtivact, ArtivactImageSet imageSet,
                                        ProgressMonitor progressMonitor) {
        return backgroundRemovalAdapter.removeBackgroundFromImages(creatorArtivact, imageSet, progressMonitor);
    }

}
