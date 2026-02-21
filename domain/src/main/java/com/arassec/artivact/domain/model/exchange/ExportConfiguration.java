package com.arassec.artivact.domain.model.exchange;

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
     * Set to {@code true} if this export is used as input for Artivact XR.
     */
    @Builder.Default
    private boolean xrExport = false;

    /**
     * Set to {@code true} to exclude items from the export.
     */
    @Builder.Default
    private boolean excludeItems = false;

}
