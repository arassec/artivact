package com.arassec.artivact.adapter.in.rest.controller.maintenance;

import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.search.ManageSearchIndexUseCase;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link MaintenanceController}.
 */
@ExtendWith(MockitoExtension.class)
class MaintenanceControllerTest {

    /**
     * Controller under test.
     */
    @InjectMocks
    private MaintenanceController maintenanceController;

    /**
     * Mock for running background operations.
     */
    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    /**
     * Mock for managing the search index.
     */
    @Mock
    private ManageSearchIndexUseCase manageSearchIndexUseCase;

    /**
     * Tests re-creating the search index as a background operation.
     */
    @Test
    void testRecreateSearchIndex() {
        maintenanceController.recreateSearchIndex();

        ArgumentCaptor<BackgroundOperation> operationCaptor = ArgumentCaptor.forClass(BackgroundOperation.class);
        verify(runBackgroundOperationUseCase).execute(eq("maintenance"), eq("search"), operationCaptor.capture());

        operationCaptor.getValue().execute(null);
        verify(manageSearchIndexUseCase).recreateIndex();
    }

}
