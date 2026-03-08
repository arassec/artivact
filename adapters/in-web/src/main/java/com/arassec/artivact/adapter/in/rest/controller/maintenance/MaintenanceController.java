package com.arassec.artivact.adapter.in.rest.controller.maintenance;

import com.arassec.artivact.adapter.in.rest.controller.BaseController;
import com.arassec.artivact.application.port.in.maintenance.CleanupProjectFilesUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.search.ManageSearchIndexUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller for maintenance operations.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController extends BaseController {

    /**
     * Use case to execute long-running background operations.
     */
    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    /**
     * Use case for managing the search index.
     */
    private final ManageSearchIndexUseCase manageSearchIndexUseCase;

    /**
     * Use case for cleaning up project files.
     */
    private final CleanupProjectFilesUseCase cleanupProjectFilesUseCase;

    /**
     * Re-creates the search index completely as a background operation.
     */
    @PostMapping("/search-index/recreate")
    public void recreateSearchIndex() {
        runBackgroundOperationUseCase.execute("maintenance", "search",
                progressMonitor -> manageSearchIndexUseCase.recreateIndex());
    }

    /**
     * Cleans up project files as a background operation.
     */
    @PostMapping("/project-files/cleanup")
    public void cleanupProjectFiles() {
        runBackgroundOperationUseCase.execute("maintenance", "cleanupProjectFiles",
                progressMonitor -> cleanupProjectFilesUseCase.cleanup());
    }

}
