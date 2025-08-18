package com.arassec.artivact.adapter.in.rest.controller.batch;

import com.arassec.artivact.application.port.in.batch.StartBatchOperationUseCase;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * Tests the {@link BatchController}.
 */
@ExtendWith(MockitoExtension.class)
class BatchControllerTest {

    /**
     * Controller under test.
     */
    @InjectMocks
    private BatchController batchController;

    /**
     * Use case to start a batch operation.
     */
    @Mock
    private StartBatchOperationUseCase startBatchOperationUseCase;

    /**
     * Tests starting a batch operation.
     */
    @Test
    void testProcess() {
        BatchProcessingParameters parameters = BatchProcessingParameters.builder().build();
        batchController.process(parameters);
        verify(startBatchOperationUseCase).process(parameters);
    }

}
