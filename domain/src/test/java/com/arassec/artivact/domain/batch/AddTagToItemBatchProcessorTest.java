package com.arassec.artivact.domain.batch;

import com.arassec.artivact.core.model.batch.BatchProcessingParameters;
import com.arassec.artivact.core.model.batch.BatchProcessingTask;
import com.arassec.artivact.core.model.configuration.ConfigurationType;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.tag.Tag;
import com.arassec.artivact.core.repository.ConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AddTagToItemBatchProcessor}.
 */
@ExtendWith(MockitoExtension.class)
class AddTagToItemBatchProcessorTest {

    /**
     * The batch processor under test.
     */
    @InjectMocks
    private AddTagToItemBatchProcessor addTagToItemBatchProcessor;

    /**
     * The configuration repository.
     */
    @Mock
    private ConfigurationRepository configurationRepository;

    /**
     * Tests processing with the batch processor is only performed for relevant tasks.
     */
    @Test
    void testProcessOnlyRelevantTask() {
        assertThat(addTagToItemBatchProcessor.process(BatchProcessingParameters.builder()
                .task(BatchProcessingTask.DELETE_ITEM).build(), new Item()))
                .isFalse();
    }

    /**
     * Tests adding a new tag.
     */
    @Test
    void testProcessTagAdded() {
        when(configurationRepository.findByType(ConfigurationType.TAGS, TagsConfiguration.class)).thenReturn(
                Optional.of(TagsConfiguration.builder()
                        .tags(List.of(
                                Tag.builder()
                                        .id("tag1")
                                        .build()
                        ))
                        .build())
        );

        Item item = new Item();

        addTagToItemBatchProcessor.initialize();

        assertThat(addTagToItemBatchProcessor.process(BatchProcessingParameters.builder()
                .task(BatchProcessingTask.ADD_TAG_TO_ITEM)
                .targetId("tag1")
                .build(), item)).isTrue();

        assertThat(item.getTags()).hasSize(1);
        assertThat(item.getTags().getFirst().getId()).isEqualTo("tag1");
    }

    /**
     * Tests skipping due to an already existing tag.
     */
    @Test
    void testProcessSkip() {
        Tag tag = Tag.builder()
                .id("tag1")
                .build();

        when(configurationRepository.findByType(ConfigurationType.TAGS, TagsConfiguration.class)).thenReturn(
                Optional.of(TagsConfiguration.builder()
                        .tags(List.of(tag))
                        .build())
        );

        Item item = new Item();
        item.getTags().add(tag);

        addTagToItemBatchProcessor.initialize();

        assertThat(addTagToItemBatchProcessor.process(BatchProcessingParameters.builder()
                .task(BatchProcessingTask.ADD_TAG_TO_ITEM)
                .targetId("tag1")
                .build(), item)).isFalse();

        assertThat(item.getTags()).hasSize(1);
        assertThat(item.getTags().getFirst().getId()).isEqualTo("tag1");
    }

}
