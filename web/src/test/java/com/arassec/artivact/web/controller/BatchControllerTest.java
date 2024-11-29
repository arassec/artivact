package com.arassec.artivact.web.controller;


import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.service.BatchService;
import com.arassec.artivact.web.model.OperationProgress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link BatchController}.
 */
@ExtendWith(MockitoExtension.class)
public class BatchControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private BatchController controller;

    /**
     * Service for batch processing.
     */
    @Mock
    private BatchService batchService;

    /**
     * Tests batch processing items.
     */
    @Test
    void testProcess() {
        ProgressMonitor progressMonitor = new ProgressMonitor(BatchService.class, "test-label-suffix");
        when(batchService.getProgressMonitor()).thenReturn(progressMonitor);

        BatchProcessingParameters parameters = BatchProcessingParameters.builder().build();

        ResponseEntity<OperationProgress> responseEntity = controller.process(parameters);

        assertThat(responseEntity.getBody()).isEqualTo(OperationProgress.builder()
                .key("Progress.BatchService.test-label-suffix")
                .currentAmount(0)
                .targetAmount(0)
                .build());

        verify(batchService).process(parameters);
    }

    /**
     * Tests querying the progress of a batch operation.
     */
    @Test
    void testGetProgress() {
        ProgressMonitor progressMonitor = new ProgressMonitor(BatchService.class, "test-label-suffix");
        progressMonitor.updateProgress(23, 42);
        progressMonitor.updateProgress("test-label-suffix-err", null);

        when(batchService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> responseEntity = controller.getProgress();

        assertThat(responseEntity.getBody()).isEqualTo(OperationProgress.builder()
                .key("Progress.BatchService.test-label-suffix-err")
                .currentAmount(23)
                .targetAmount(42)
                .error(null)
                .build());
    }

}
