package com.arassec.artivact.application.service.operation;

import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackgroundOperationServiceTest {

    @Mock
    ExecutorService executorService;

    @InjectMocks
    BackgroundOperationService service;

    @Test
    void testExecuteRunsBackgroundOperation() {
        BackgroundOperation operation = mock(BackgroundOperation.class);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executorService).submit(any(Runnable.class));

        service.execute("topic1", "step1", operation);

        verify(operation, times(1)).execute(any(ProgressMonitor.class));

        ProgressMonitor progress = service.getProgress();
        assertThat(progress).isNull();
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testExecuteDoesNotRunIfAlreadyRunning() {
        CountDownLatch latch = new CountDownLatch(1);

        BackgroundOperation firstOperation = progressMonitor -> {
            try {
                // block second operation... ignore result of await
                latch.await(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread waiting too long!", e);
            }
        };

        BackgroundOperation secondOperation =
                progressMonitor -> fail("Second operation should not be executed!");

        service.execute("topic1", "step1", firstOperation);
        service.execute("topic2", "step2", secondOperation);

        latch.countDown();
    }

    @Test
    void testExecuteHandlesException() {
        BackgroundOperation operation = pm -> {
            throw new RuntimeException("fail");
        };

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executorService).submit(any(Runnable.class));

        service.execute("topic1", "step1", operation);

        ProgressMonitor progress = service.getProgress();
        assertThat(progress).isNotNull();
        assertThat(progress.getException()).isInstanceOf(RuntimeException.class)
                .hasMessage("fail");
    }
}
