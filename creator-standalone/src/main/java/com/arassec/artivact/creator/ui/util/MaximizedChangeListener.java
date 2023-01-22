package com.arassec.artivact.creator.ui.util;

import com.arassec.artivact.creator.core.service.ConfigurationService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaximizedChangeListener implements ChangeListener<Boolean> {

    private final ConfigurationService configurationService;

    @Setter
    private boolean inProject;

    @Setter
    private Stage stage;

    @Override
    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
        if (inProject) {
            configurationService.saveMaximized(newValue);
            if (Boolean.FALSE.equals(newValue)) {
                stage.setWidth(1024);
                stage.setHeight(768);
            }
        }
    }

}
