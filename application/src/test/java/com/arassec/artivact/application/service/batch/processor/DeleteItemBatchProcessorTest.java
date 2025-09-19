package com.arassec.artivact.application.service.batch.processor;

import com.arassec.artivact.application.service.item.ManageItemService;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.model.batch.BatchProcessingTask;
import com.arassec.artivact.domain.model.item.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link DeleteItemBatchProcessor}.
 */
@ExtendWith(MockitoExtension.class)
class DeleteItemBatchProcessorTest {

    /**
     * The processor under test.
     */
    @InjectMocks
    private DeleteItemBatchProcessor deleteItemBatchProcessor;

    /**
     * The service for item manipulation.
     */
    @Mock
    private ManageItemService itemService;

    /**
     * Tests processing with the batch processor is only performed for relevant tasks.
     */
    @Test
    void testProcessOnlyRelevantTask() {
        deleteItemBatchProcessor.initialize();

        assertThat(deleteItemBatchProcessor.process(BatchProcessingParameters.builder()
                .task(BatchProcessingTask.ADD_TAG_TO_ITEM).build(), new Item()))
                .isFalse();
    }

    /**
     * Tests processing.
     */
    @Test
    void testProcess() {
        Item item = new Item();
        item.setId("item-id");

        assertThat(deleteItemBatchProcessor.process(BatchProcessingParameters.builder()
                .task(BatchProcessingTask.DELETE_ITEM)
                .build(), item)).isFalse();

        verify(itemService).delete("item-id");
    }

}
