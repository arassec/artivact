package com.arassec.artivact.application.service.batch;

import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.search.SearchItemsUseCase;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.model.batch.BatchProcessingTask;
import com.arassec.artivact.domain.model.batch.BatchProcessor;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    private SearchItemsUseCase searchItemsUseCase;

    @Mock
    private LoadItemUseCase loadItemUseCase;

    @Mock
    private SaveItemUseCase saveItemUseCase;

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @Mock
    private BatchProcessor batchProcessor;

    @InjectMocks
    private BatchService batchService;

    private BatchProcessingParameters params;

    @BeforeEach
    void setUp() {
        params = new BatchProcessingParameters();
        batchService = new BatchService(
                searchItemsUseCase,
                loadItemUseCase,
                saveItemUseCase,
                List.of(batchProcessor),
                runBackgroundOperationUseCase
        );
    }

    @Test
    void testProcessDoesNothingIfSearchTermIsEmptyAndTaskIsNotUploadModified() {
        params.setSearchTerm("");
        params.setTask(BatchProcessingTask.UPDATE_SEARCH_INDEX);
        params.setMaxItems(10);

        batchService.process(params);

        verifyNoInteractions(runBackgroundOperationUseCase);
    }

    @Test
    void testProcessDoesNothingIfTaskIsNull() {
        params.setSearchTerm("test");
        params.setTask(null);
        params.setMaxItems(10);

        batchService.process(params);

        verifyNoInteractions(runBackgroundOperationUseCase);
    }

    @Test
    void testProcessDoesNothingIfMaxItemsIsZero() {
        params.setSearchTerm("test");
        params.setTask(BatchProcessingTask.UPDATE_SEARCH_INDEX);
        params.setMaxItems(0);

        batchService.process(params);

        verifyNoInteractions(runBackgroundOperationUseCase);
    }

    @Test
    void testProcessStopsIfExclusiveProcessorReturnsTrue() {
        params.setSearchTerm("test");
        params.setTask(BatchProcessingTask.UPDATE_SEARCH_INDEX);
        params.setMaxItems(5);

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        when(batchProcessor.processAllExclusive(params)).thenReturn(true);

        batchService.process(params);

        verify(batchProcessor).initialize();
        verify(batchProcessor).processAllExclusive(params);
        verifyNoInteractions(searchItemsUseCase, loadItemUseCase, saveItemUseCase);
    }

    @Test
    void testProcessHandlesUploadModifiedItems() {
        params.setTask(BatchProcessingTask.UPLOAD_MODIFIED_ITEM);
        params.setMaxItems(2);

        Item item = new Item();
        when(loadItemUseCase.loadModified(2)).thenReturn(List.of(item));
        when(batchProcessor.process(params, item)).thenReturn(true);

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        batchService.process(params);

        verify(batchProcessor).initialize();
        verify(loadItemUseCase).loadModified(2);
        verify(batchProcessor).process(params, item);
        verify(saveItemUseCase).save(item);
    }

    @Test
    void testProcessHandlesSearchItems() {
        params.setTask(BatchProcessingTask.UPDATE_SEARCH_INDEX);
        params.setSearchTerm("query");
        params.setMaxItems(3);

        Item item = new Item();
        when(searchItemsUseCase.search("query", 3)).thenReturn(List.of(item));
        when(batchProcessor.process(params, item)).thenReturn(true);

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        batchService.process(params);

        verify(batchProcessor).initialize();
        verify(searchItemsUseCase).search("query", 3);
        verify(batchProcessor).process(params, item);
        verify(saveItemUseCase).save(item);
    }

    @Test
    void testProcessDoesNotSaveIfNoProcessorHandlesItem() {
        params.setTask(BatchProcessingTask.UPDATE_SEARCH_INDEX);
        params.setSearchTerm("query");
        params.setMaxItems(1);

        Item item = new Item();
        when(searchItemsUseCase.search("query", 1)).thenReturn(List.of(item));
        when(batchProcessor.process(params, item)).thenReturn(false);

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        batchService.process(params);

        verify(searchItemsUseCase).search("query", 1);
        verify(batchProcessor).process(params, item);
        verify(saveItemUseCase, never()).save(item);
    }

    @Test
    void testProcessRunsWithoutExceptions() {
        params.setTask(BatchProcessingTask.UPDATE_SEARCH_INDEX);
        params.setSearchTerm("query");
        params.setMaxItems(1);

        when(searchItemsUseCase.search("query", 1)).thenReturn(Collections.emptyList());

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        assertThatCode(() -> batchService.process(params))
                .doesNotThrowAnyException();
    }

}
