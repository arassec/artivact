package com.arassec.artivact.core.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for a content export.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportConfiguration {

    /**
     * Set to {@code true} to filter restricted content from exports.
     */
    @Builder.Default
    private boolean applyRestrictions = false;

    /**
     * Set to {@code true} to export the minimal set of media files to reduce export size.
     */
    @Builder.Default
    private boolean optimizeSize = false;

    /**
     * Set to {@code true} to exclude items from the export.
     */
    @Builder.Default
    private boolean excludeItems = false;

}
