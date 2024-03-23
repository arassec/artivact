package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.util.FileUtil;
import com.arassec.artivact.backend.service.misc.ProjectDataProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Fallback model creator that creates a default 3D model.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class FallbackModelCreatorAdapter extends BaseModelCreatorAdapter {

    /**
     * The file util.
     */
    private final FileUtil fileUtil;

    /**
     * The project root provider.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.FALLBACK_MODEL_CREATOR_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images) {
        log.info("Fallback model creator called.");

        Path tempDir = initParams.getTempDir();

        fileUtil.emptyDir(tempDir);

        try {
            Files.copy(projectDataProvider.getProjectRoot().resolve("utils/fallback-model.obj"), tempDir);
            Files.copy(projectDataProvider.getProjectRoot().resolve("utils/fallback-model.mtl"), tempDir);
        } catch (IOException e) {
            throw new ArtivactException("Could not copy fallback model files!", e);
        }

        return new ModelCreationResult(tempDir, "fallback");
    }

}
