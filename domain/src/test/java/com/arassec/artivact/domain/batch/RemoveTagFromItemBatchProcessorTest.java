package com.arassec.artivact.domain.batch;

import com.arassec.artivact.core.model.batch.BatchProcessingParameters;
import com.arassec.artivact.core.model.batch.BatchProcessingTask;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.tag.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link RemoveTagFromItemBatchProcessor}.
 */
class RemoveTagFromItemBatchProcessorTest {

    /**
     * The processor under test.
     */
    private final RemoveTagFromItemBatchProcessor removeTagFromItemBatchProcessor = new RemoveTagFromItemBatchProcessor();

    /**
     * Tests processing with the batch processor is only performed for relevant tasks.
     */
    @Test
    void testProcessOnlyRelevantTask() {
        assertThat(removeTagFromItemBatchProcessor.process(BatchProcessingParameters.builder()
                .task(BatchProcessingTask.DELETE_ITEM).build(), new Item()))
                .isFalse();
    }

    /**
     * Tests processing if the tag isn't already on the item.
     */
    @Test
    void testProcessTagNotOnItem() {
        Item item = new Item();

        assertThat(removeTagFromItemBatchProcessor.process(BatchProcessingParameters.builder()
                .task(BatchProcessingTask.REMOVE_TAG_FROM_ITEM)
                .targetId("tag-id")
                .build(), item)).isFalse();
    }

    /**
     * Tests removing a tag from an item.
     */
    @Test
    void testProcess() {
        Item item = new Item();
        item.getTags().add(Tag.builder().id("tag-id").build());

        assertThat(removeTagFromItemBatchProcessor.process(BatchProcessingParameters.builder()
                .task(BatchProcessingTask.REMOVE_TAG_FROM_ITEM)
                .targetId("tag-id")
                .build(), item)).isTrue();

        assertThat(item.getTags()).isEmpty();
    }

}
