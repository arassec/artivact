package com.arassec.artivact.application.batch;

import com.arassec.artivact.application.port.in.item.UploadItemUseCase;
import com.arassec.artivact.application.service.batch.processor.UploadModifiedItemBatchProcessor;
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
 * Tests the {@link UploadModifiedItemBatchProcessorTest}.
 */
@ExtendWith(MockitoExtension.class)
class UploadModifiedItemBatchProcessorTest {

    /**
     * The processor under test.
     */
    @InjectMocks
    private UploadModifiedItemBatchProcessor uploadModifiedItemBatchProcessor;

    /**
     * The Use-Case for item upload.
     */
    @Mock
    private UploadItemUseCase uploadItemUseCase;

    /**
     * Tests processing with the batch processor is only performed for relevant tasks.
     */
    @Test
    void testProcessOnlyRelevantTask() {
        uploadModifiedItemBatchProcessor.initialize();

        assertThat(uploadModifiedItemBatchProcessor.process(BatchProcessingParameters.builder()
                .task(BatchProcessingTask.DELETE_ITEM).build(), new Item()))
                .isFalse();
    }

    /**
     * Tests uploading an item.
     */
    @Test
    void testProcess() {
        Item item = new Item();
        item.setId("123-abc");
        item.setVersion(6);

        assertThat(uploadModifiedItemBatchProcessor.process(BatchProcessingParameters.builder()
                .task(BatchProcessingTask.UPLOAD_MODIFIED_ITEM).build(), item))
                .isTrue();

        verify(uploadItemUseCase).uploadItemToRemoteInstance("123-abc", false);

        assertThat(item.getSyncVersion()).isEqualTo(7);
    }

}
