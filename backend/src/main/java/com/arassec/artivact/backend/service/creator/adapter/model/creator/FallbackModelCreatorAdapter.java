package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.util.FileUtil;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
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
     * The project root provider.
     */
    private final ProjectRootProvider projectRootProvider;

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
    public ModelCreationResult createModel(List<Path> images, String pipeline) {
        log.info("Fallback model creator called with pipeline: {}", pipeline);

        Path tempDir = initParams.getTempDir();

        fileUtil.emptyDir(tempDir);

        try {
            Files.copy(projectRootProvider.getProjectRoot().resolve("utils/fallback-model.obj"), tempDir);
            Files.copy(projectRootProvider.getProjectRoot().resolve("utils/fallback-model.mtl"), tempDir);
        } catch (IOException e) {
            throw new ArtivactException("Could not copy fallback model files!", e);
        }

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
