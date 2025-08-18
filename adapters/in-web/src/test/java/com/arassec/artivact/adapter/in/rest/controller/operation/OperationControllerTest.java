package com.arassec.artivact.adapter.in.rest.controller.operation;

import com.arassec.artivact.adapter.in.rest.model.OperationProgress;
import com.arassec.artivact.application.port.in.operation.GetBackgroundOperationProgressUseCase;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link OperationController}.
 */
@ExtendWith(MockitoExtension.class)
class OperationControllerTest {

    /**
     * Controller under test.
     */
    @InjectMocks
    private OperationController operationController;

    /**
     * Use case to get the progress of a long-running background operation.
     */
    @Mock
    private GetBackgroundOperationProgressUseCase getBackgroundOperationProgressUseCase;

    /**
     * Tests retrieving the progress of an operation.
     */
    @Test
    void testGetProgress() {
        ProgressMonitor progressMonitor = new ProgressMonitor("topic", "step");
        progressMonitor.updateProgress(23, 100);

        when(getBackgroundOperationProgressUseCase.getProgress()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> progressResponseEntity = operationController.getProgress();

        OperationProgress operationProgress = progressResponseEntity.getBody();
        assertThat(operationProgress).isNotNull();
        assertThat(operationProgress.getKey()).isEqualTo("Progress.topic.step");
        assertThat(operationProgress.getCurrentAmount()).isEqualTo(23);
        assertThat(operationProgress.getTargetAmount()).isEqualTo(100);
    }

    /**
     * Tests retrieving the progress of a failed operation.
     */
    @Test
    void testGetProgressFailedOperation() {
        ProgressMonitor progressMonitor = new ProgressMonitor("topic", "step");
        progressMonitor.updateProgress(23, 100);
        progressMonitor.updateProgress("failed", new ArtivactException("test-exception"));

        when(getBackgroundOperationProgressUseCase.getProgress()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> progressResponseEntity = operationController.getProgress();

        OperationProgress operationProgress = progressResponseEntity.getBody();
        assertThat(operationProgress).isNotNull();
        assertThat(operationProgress.getKey()).isEqualTo("Progress.topic.failed");
        assertThat(operationProgress.getCurrentAmount()).isEqualTo(23);
        assertThat(operationProgress.getTargetAmount()).isEqualTo(100);
        assertThat(operationProgress.getError()).startsWith("com.arassec.artivact.domain.exception.ArtivactException: test-exception");
    }

}
