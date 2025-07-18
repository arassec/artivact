package com.arassec.artivact.application.service.batch.processor;

import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.model.batch.BatchProcessingTask;
import com.arassec.artivact.domain.model.batch.BatchProcessor;
import com.arassec.artivact.domain.model.configuration.ConfigurationType;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.tag.Tag;
import com.arassec.artivact.application.port.out.repository.ConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Batch processor that adds a tag to an item if it isn't already present there.
 */
@Component
@RequiredArgsConstructor
public class AddTagToItemBatchProcessor implements BatchProcessor {

    /**
     * Repository for configurations.
     */
    private final ConfigurationRepository configurationRepository;

    /**
     * The tags configuration.
     */
    private TagsConfiguration tagsConfiguration;

    /**
     * Loads the current tags configuration.
     */
    @Override
    public void initialize() {
        tagsConfiguration = configurationRepository.findByType(ConfigurationType.TAGS, TagsConfiguration.class).orElseThrow();
    }

    /**
     * Adds the requested tag to the item if necessary.
     *
     * @param params The parameters for batch processing an item.
     * @param item   The item to process.
     */
    @Override
    public boolean process(BatchProcessingParameters params, Item item) {
        if (!BatchProcessingTask.ADD_TAG_TO_ITEM.equals(params.getTask())) {
            return false;
        }

        String tagId = params.getTargetId();

        Optional<Tag> tagOnItem = item.getTags().stream()
                .filter(tag -> tagId.equals(tag.getId()))
                .findFirst();

        if (tagOnItem.isPresent()) {
            return false;
        }

        tagsConfiguration.getTags().stream()
                .filter(tag -> tag.getId().equals(tagId))
                .findFirst()
                .ifPresent(tag -> item.getTags().add(tag));

        return true;
    }

}
