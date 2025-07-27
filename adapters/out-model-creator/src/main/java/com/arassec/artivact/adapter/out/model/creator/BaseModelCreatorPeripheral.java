package com.arassec.artivact.adapter.out.model.creator;

import com.arassec.artivact.application.port.out.peripheral.ModelCreatorPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.BasePeripheralAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for model-creator adapter implementations.
 */
@Slf4j
public abstract class BaseModelCreatorPeripheral extends BasePeripheralAdapter implements ModelCreatorPeripheral {

    /**
     * The export subdirectory where model creators should export their results in.
     */
    protected static final String EXPORT_DIR = "export/";

    /**
     * Returns the repository for file access.
     *
     * @return The {@link FileRepository}.
     */
    protected abstract FileRepository getFileRepository();

    /**
     * Copies all images from the provided {@link CreationImageSet} to the provided destination.
     *
     * @param images          The images to copy.
     * @param destination     The destination path to copy the images to.
     * @param progressMonitor A progress monitor which is updated during copying.
     */
    protected void copyImages(List<Path> images, Path destination, ProgressMonitor progressMonitor) {
        progressMonitor.updateLabelKey("copyImages");
        var index = new AtomicInteger(1);
        images.forEach(image -> {
            progressMonitor.updateProgress(index.getAndIncrement(), images.size());
            getFileRepository().copy(image, destination.resolve(image.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        });
    }

}
