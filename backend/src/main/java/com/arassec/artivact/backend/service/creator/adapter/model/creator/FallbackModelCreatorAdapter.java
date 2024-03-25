package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.misc.ProjectDataProvider;
import com.arassec.artivact.backend.service.util.FileUtil;
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
        Path resultDir = initParams.getTempDir().resolve(EXPORT_DIR);
        fileUtil.emptyDir(resultDir);
        fileUtil.createDirIfRequired(resultDir);
        return new ModelCreationResult(resultDir, "fallback");
    }

}
