package com.arassec.artivact.adapter.out.model.creator.peripheral;

import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;
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
public class FallbackModelCreatorPeripheral extends BaseModelCreatorPeripheral {

    /**
     * The file repository.
     */
    private final FileRepository fileRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.FALLBACK_MODEL_CREATOR_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images) {
        log.info("Fallback model creator called.");
        Path resultDir = initParams.getWorkDir().resolve(EXPORT_DIR);
        fileRepository.emptyDir(resultDir);
        fileRepository.createDirIfRequired(resultDir);
        return new ModelCreationResult(resultDir, "fallback");
    }

}
