package com.arassec.artivact.domain.export;

import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.domain.export.model.ExportParams;
import com.arassec.artivact.domain.export.model.ExportType;

/**
 * Defines an exporter of Artivact's data.
 */
public interface ArtivactExporter {

    /**
     * File suffix for the main export file.
     */
    String CONTENT_EXPORT_FILE_SUFFIX = "artivact.content.json";

    /**
     * Returns the supported export type.
     *
     * @return The {@link ExportType} this exporter supports.
     */
    ExportType supports();

    /**
     * Exports the given menu and all its content.
     *
     * @param exportParams    Export parameters.
     * @param menu            The menu to export.
     * @param progressMonitor The progress monitor.
     */
    void export(ExportParams exportParams, Menu menu, ProgressMonitor progressMonitor);

}
