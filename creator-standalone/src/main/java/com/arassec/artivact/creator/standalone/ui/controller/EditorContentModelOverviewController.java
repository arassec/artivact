package com.arassec.artivact.creator.standalone.ui.controller;

import com.arassec.artivact.creator.standalone.core.service.ProjectService;
import com.arassec.artivact.creator.standalone.ui.event.EditorEventType;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EditorContentModelOverviewController extends EditorContentBaseController {

    @FXML
    private FlowPane modelOverviewContentPane;

    public EditorContentModelOverviewController(ProjectService projectService, MessageSource messageSource) {
        super(projectService, messageSource);
    }

    @FXML
    public void initialize() {
        initialize(modelOverviewContentPane, EditorEventType.MODEL_OVERVIEW_SELECTED, "#editorContentModelOverviewPane");
    }

    protected void updateContent(int imageSetIndex) {
        // Nothing to do here at the moment...
    }

}
