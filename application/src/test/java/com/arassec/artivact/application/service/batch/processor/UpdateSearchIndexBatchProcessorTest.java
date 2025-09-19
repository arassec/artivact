package com.arassec.artivact.application.service.batch.processor;

import com.arassec.artivact.application.port.in.search.ManageSearchIndexUseCase;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.model.batch.BatchProcessingTask;
import com.arassec.artivact.domain.model.item.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UpdateSearchIndexBatchProcessorTest {

    @Mock
    private ManageSearchIndexUseCase manageSearchIndexUseCase;

    @InjectMocks
    private UpdateSearchIndexBatchProcessor processor;

    private BatchProcessingParameters params;

    @BeforeEach
    void setUp() {
        params = new BatchProcessingParameters();
    }

    @Test
    void processAllExclusiveWrongTask() {
        params.setTask(BatchProcessingTask.DELETE_ITEM);

        boolean result = processor.processAllExclusive(params);

        assertThat(result).isFalse();
        verifyNoInteractions(manageSearchIndexUseCase);
    }

    @Test
    void processAllExclusiveRecreateIndex() {
        params.setTask(BatchProcessingTask.UPDATE_SEARCH_INDEX);
        params.setSearchTerm("*");

        boolean result = processor.processAllExclusive(params);

        assertThat(result).isTrue();
        verify(manageSearchIndexUseCase).recreateIndex();
    }

    @Test
    void processAllExclusiveWrongSearchTerm() {
        params.setTask(BatchProcessingTask.UPDATE_SEARCH_INDEX);
        params.setSearchTerm("abc");

        boolean result = processor.processAllExclusive(params);

        assertThat(result).isFalse();
        verifyNoInteractions(manageSearchIndexUseCase);
    }

    @Test
    void processWrongTask() {
        params.setTask(BatchProcessingTask.DELETE_ITEM);
        Item item = new Item();

        boolean result = processor.process(params, item);

        assertThat(result).isFalse();
        verifyNoInteractions(manageSearchIndexUseCase);
    }

    @Test
    void processUpdateIndex() {
        params.setTask(BatchProcessingTask.UPDATE_SEARCH_INDEX);
        Item item = new Item();

        boolean result = processor.process(params, item);

        assertThat(result).isTrue();
        verify(manageSearchIndexUseCase).updateIndex(item);
    }

}
