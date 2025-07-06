package com.arassec.artivact.domain.model.appearance;

import lombok.Data;

/**
 * Color theme of the application.
 */
@Data
public class ColorTheme {

    /**
     * Primary color.
     */
    private String primary;

    /**
     * Secondary color.
     */
    private String secondary;

    /**
     * Accent color.
     */
    private String accent;

    /**
     * Dark color.
     */
    private String dark;

    /**
     * Color for positive feedback.
     */
    private String positive;

    /**
     * Color for negative feedback.
     */
    private String negative;

    /**
     * Color for info-level feedback.
     */
    private String info;

    /**
     * Color for warnings.
     */
    private String warning;

}
