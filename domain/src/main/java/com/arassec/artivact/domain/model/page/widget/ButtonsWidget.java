package com.arassec.artivact.domain.model.page.widget;

import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.WidgetType;
import com.arassec.artivact.domain.model.page.widget.model.ButtonConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Widget displaying one or more buttons in a grid of configurable columns.
 */
@Getter
@Setter
public class ButtonsWidget extends Widget {

    /**
     * Number of columns in the grid.
     */
    private int columns;

    /**
     * The button configurations.
     */
    private List<ButtonConfig> buttonConfigs;

    /**
     * Creates a new instance.
     */
    public ButtonsWidget() {
        super(WidgetType.BUTTONS);
    }

}
