package com.arassec.artivact.creator.standalone.core.service;

import com.arassec.artivact.creator.standalone.core.adapter.model.creator.ModelCreatorAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.model.editor.ModelEditorAdapter;
import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactAsset;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelCreatorAdapter modelCreatorAdapter;

    private final ModelEditorAdapter modelEditorAdapter;

    public String getDefaultPipeline() {
        return modelCreatorAdapter.getDefaultPipeline();
    }

    public List<String> getPipelines() {
        return modelCreatorAdapter.getPipelines();
    }

    public void createModel(CreatorArtivact creatorArtivact, String pipeline, ProgressMonitor progressMonitor) {
        modelCreatorAdapter.createModel(creatorArtivact, pipeline, progressMonitor);
    }

    public void cancelModelCreation() {
        modelCreatorAdapter.cancelModelCreation();
    }

    public void openModel(CreatorArtivact creatorArtivact, ArtivactAsset asset) {
        modelEditorAdapter.openModel(creatorArtivact, asset);
    }

    public boolean cancelModelCreationSupported() {
        return modelCreatorAdapter.supportsCancellation();
    }

}
