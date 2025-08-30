package com.arassec.artivact.domain.model.page.widget.model;

import com.arassec.artivact.domain.model.TranslatableString;
import lombok.Data;

/**
 * Configuration of a button displayed in the {@link com.arassec.artivact.domain.model.page.widget.ButtonsWidget}.
 */
@Data
public class ButtonConfig {

    /**
     * The URL the button leads to on click.
     */
    private String targetUrl;

    /**
     * An optional icon displayed left on the button.
     */
    private String iconLeft;

    /**
     * The buttons label.
     */
    private TranslatableString label;

    /**
     * An optional icon displayed right on the button.
     */
    private String iconRight;

    /**
     * The button's size.
     */
    private int size;

    /**
     * The button's color.
     */
    private String buttonColor;

    /**
     * The button text's color.
     */
    private String textColor;

}
