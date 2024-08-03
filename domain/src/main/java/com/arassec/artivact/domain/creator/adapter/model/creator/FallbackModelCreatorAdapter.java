package com.arassec.artivact.domain.creator.adapter.model.creator;

import com.arassec.artivact.core.model.configuration.AdapterImplementation;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
     * The file repository.
     */
    private final FileRepository fileRepository;

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
        Path resultDir = initParams.getTempDir().resolve(EXPORT_DIR);
        fileRepository.emptyDir(resultDir);
        fileRepository.createDirIfRequired(resultDir);
        return new ModelCreationResult(resultDir, "fallback");
    }

}
