package com.arassec.artivact.application.service.batch.processor;

import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.model.batch.BatchProcessingTask;
import com.arassec.artivact.domain.model.batch.BatchProcessor;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.tag.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * {@link BatchProcessor} to remove a tag from an item.
 */
@Component
@RequiredArgsConstructor
public class RemoveTagFromItemBatchProcessor implements BatchProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        // Nothing to do here.
    }

    /**
     * Removes a tag from an item if necessary.
     *
     * @param params The parameters for batch processing an item.
     * @param item   The item to process.
     */
    @Override
    public boolean process(BatchProcessingParameters params, Item item) {
        if (!BatchProcessingTask.REMOVE_TAG_FROM_ITEM.equals(params.getTask())) {
            return false;
        }

        String tagId = params.getTargetId();

        Optional<Tag> tagOnItem = item.getTags().stream()
                .filter(tag -> tag.getId().equals(tagId))
                .findFirst();

        tagOnItem.ifPresent(tag -> item.getTags().remove(tag));

        return tagOnItem.isPresent();
    }

}
