package com.arassec.artivact.application.service.operation;

import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class BackgroundOperationServiceTest {

    @Test
    void testExecuteRunsBackgroundOperation() throws Exception {
        BackgroundOperationService service = new BackgroundOperationService();

        BackgroundOperation op = mock(BackgroundOperation.class);

        service.execute("topic1", "step1", op);

        // Give the thread some time to finish:
        TimeUnit.MILLISECONDS.sleep(200);

        verify(op).execute(any(ProgressMonitor.class));
        assertThat(service.getProgress()).isNull(); // nach Ende wird Monitor auf null gesetzt
    }

    @Test
    void testExecuteSkippedIfAlreadyRunning() {
        BackgroundOperationService service = new BackgroundOperationService();

        BackgroundOperation op1 = mock(BackgroundOperation.class);
        BackgroundOperation op2 = mock(BackgroundOperation.class);

        service.execute("topicA", "stepA", op1);

        ProgressMonitor running = service.getProgress();
        assertThat(running).isNotNull();

        service.execute("topicB", "stepB", op2);

        verify(op2, never()).execute(any());
    }

    @Test
    void testExecuteWithException() throws Exception {
        BackgroundOperationService service = new BackgroundOperationService();

        BackgroundOperation op = mock(BackgroundOperation.class);
        doThrow(new RuntimeException("fail!")).when(op).execute(any());

        service.execute("topicX", "stepX", op);

        // Give the thread some time to finish:
        TimeUnit.MILLISECONDS.sleep(200);

        ProgressMonitor progress = service.getProgress();
        assertThat(progress).isNotNull();
        assertThat(progress.getException()).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testTeardownShutsDownExecutor() {
        BackgroundOperationService service = new BackgroundOperationService();
        assertDoesNotThrow(service::teardown);
    }

}
