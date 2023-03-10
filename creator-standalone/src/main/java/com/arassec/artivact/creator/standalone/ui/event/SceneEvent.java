package com.arassec.artivact.creator.standalone.ui.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class SceneEvent extends ApplicationEvent {

    public static final String PROJECT_CHOOSER_FXML = "/fxml/project-chooser.fxml";
    public static final String PROJECT_HOME_FXML = "/fxml/project-home.fxml";
    public static final String ARTIVACT_EDITOR_FXML = "/fxml/editor.fxml";

    @Getter
    private final SceneEventType type;

    @Getter
    private final SceneConfig config;

    @Getter
    private final String statusKey;

    public SceneEvent(SceneEventType type, SceneConfig config, String statusKey) {
        super(type);
        this.type = type;
        this.config = config;
        this.statusKey = statusKey;
    }

}
