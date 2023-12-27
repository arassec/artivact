package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.util.CmdUtil;
import com.arassec.artivact.backend.service.util.FileUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Adapter implementation that uses "Metashape" for model creation.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class MetashapeModelCreatorAdapter extends BaseModelCreatorAdapter {

    /**
     * The implementation supported by this adapter.
     */
    private final AdapterImplementation supportedImplementation = AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER;

    /**
     * The commandline util.
     */
    private final CmdUtil cmdUtil;

    /**
     * The file util.
     */
    private final FileUtil fileUtil;

    /**
     * Message source for I18N.
     */
    private final MessageSource messageSource;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getDefaultPipeline() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPipelines() {
        return List.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images, String pipeline) {
        Path tempDir = initParams.getTempDir();

        Path resultDir = tempDir.resolve("metashape-export/").toAbsolutePath();

        fileUtil.emptyDir(tempDir);

        copyImages(images, tempDir, progressMonitor,
                messageSource.getMessage("base-adapter.copy-images.progress.prefix", null, Locale.getDefault()));

        fileUtil.openDirInOs(tempDir);

        progressMonitor.updateProgress(messageSource.getMessage("model-creator-adapter.metashape.progress.prefix", null, Locale.getDefault()));

        cmdUtil.execute(new CommandLine(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation())));

        progressMonitor.updateProgress(messageSource.getMessage("model-creator-adapter.metashape.import.progress.prefix", null, Locale.getDefault()));

        return new ModelCreationResult(resultDir, "metashape");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelModelCreation() {
        throw new ArtivactException("Cancellation is not supported by " + this.getClass().getSimpleName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsCancellation() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration(AdapterConfiguration adapterConfiguration) {
        if (!cmdUtil.execute(new CommandLine(adapterConfiguration.getConfigValue(getSupportedImplementation())))) {
            throw new ArtivactException("Configured file could not be executed!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean healthy(AdapterConfiguration adapterConfiguration) {
        try {
            return fileUtil.isFileExecutable(adapterConfiguration.getConfigValue(getSupportedImplementation()));
        } catch (Exception e) {
            return false;
        }
    }

}
