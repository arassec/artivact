package com.arassec.artivact.application.service.batch.processor;

import com.arassec.artivact.application.port.in.item.UploadItemUseCase;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.model.batch.BatchProcessingTask;
import com.arassec.artivact.domain.model.batch.BatchProcessor;
import com.arassec.artivact.domain.model.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * {@link BatchProcessor} to upload a modified item to a remote Artivact instance.
 */
@Component
@RequiredArgsConstructor
public class UploadModifiedItemBatchProcessor implements BatchProcessor {

    /**
     * Use-Case for uploading items to remote Artivact instances.
     */
    private final UploadItemUseCase uploadItemUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        // Nothing to do here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(BatchProcessingParameters params, Item item) {
        if (BatchProcessingTask.UPLOAD_MODIFIED_ITEM.equals(params.getTask())) {
            uploadItemUseCase.uploadItemToRemoteInstance(item.getId(), false);
            item.setSyncVersion(item.getVersion() + 1); // Saving the item later increments the "version" property!
            return true;
        }
        return false;
    }

}
