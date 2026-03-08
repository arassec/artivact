package com.arassec.artivact.application.service.maintenance;

import com.arassec.artivact.application.port.in.maintenance.CleanupProjectFilesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for cleanup project files operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupProjectFilesService implements CleanupProjectFilesUseCase {

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        log.info("Cleaning up project files.");
    }

}
