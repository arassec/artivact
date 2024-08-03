package com.arassec.artivact.domain.creator.adapter.model.creator;

import com.arassec.artivact.core.model.configuration.AdapterImplementation;
import com.arassec.artivact.core.repository.FileRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Adapter implementation that uses "Metashape" for model creation.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class MetashapeModelCreatorAdapter extends BaseModelCreatorAdapter {

    /**
     * The file repository.
     */
    private final FileRepository fileRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images) {
        Path tempDir = initParams.getTempDir();

        Path resultDir = tempDir.resolve(EXPORT_DIR).toAbsolutePath();

        fileRepository.emptyDir(tempDir);
        fileRepository.createDirIfRequired(resultDir);

        copyImages(images, tempDir, progressMonitor);

        fileRepository.openDirInOs(tempDir);

        progressMonitor.updateLabelKey("createModelStart");

        execute(new CommandLine(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation())));

        return new ModelCreationResult(resultDir, "metashape");
    }

}
