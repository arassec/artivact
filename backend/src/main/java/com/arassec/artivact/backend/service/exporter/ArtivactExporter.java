package com.arassec.artivact.backend.service.exporter;

import com.arassec.artivact.backend.service.exporter.model.ExportParams;
import com.arassec.artivact.backend.service.exporter.model.ExportType;
import com.arassec.artivact.backend.service.misc.ProgressMonitor;
import com.arassec.artivact.backend.service.model.menu.Menu;

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
