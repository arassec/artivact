package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.model.item.asset.ImageSet;
import com.arassec.artivact.backend.service.util.FileUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Fallback model creator that creates a default 3D model.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class FallbackModelCreatorAdapter extends BaseModelCreatorAdapter {

    /**
     * The implementation supported by this adapter.
     */
    private final AdapterImplementation supportedImplementation = AdapterImplementation.FALLBACK_MODEL_CREATOR_ADAPTER;

    /**
     * The file util.
     */
    private final FileUtil fileUtil;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getDefaultPipeline() {
        return Optional.of("fallback-default");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPipelines() {
        return List.of("fallback-default", "fallback-extended");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<ImageSet> imageSets, String pipeline) {
        log.info("Fallback model creator called with pipeline: {}", pipeline);

        Path tempDir = initParams.getTempDir();

        fileUtil.emptyDir(tempDir);

        fileUtil.copyClasspathResource(Path.of("project-setup/Utils/fallback-model.obj"), tempDir);
        fileUtil.copyClasspathResource(Path.of("project-setup/Utils/fallback-model.mtl"), tempDir);

        return new ModelCreationResult(tempDir, pipeline);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelModelCreation() {
        log.info("Fallback model creator called to cancel model creation.");
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean healthy(AdapterConfiguration adapterConfiguration) {
        return true;
    }

}
