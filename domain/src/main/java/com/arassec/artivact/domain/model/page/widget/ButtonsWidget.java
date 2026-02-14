package com.arassec.artivact.domain.model.page.widget;

import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.WidgetType;
import com.arassec.artivact.domain.model.page.widget.model.ButtonConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
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
    private List<ButtonConfig> buttonConfigs = new LinkedList<>();

    /**
     * Creates a new instance.
     */
    public ButtonsWidget() {
        super(WidgetType.BUTTONS);
    }

    /**
     * Creates a new instance.
     *
     * @param type          The widget type.
     * @param columns       Number of columns in the grid.
     * @param buttonConfigs The button configurations.
     */
    public ButtonsWidget(WidgetType type, int columns, List<ButtonConfig> buttonConfigs) {
        super(type);
        this.columns = columns;
        if (buttonConfigs != null) {
            this.buttonConfigs = buttonConfigs;
        }
    }

}
