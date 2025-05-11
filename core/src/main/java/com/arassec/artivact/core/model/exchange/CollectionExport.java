package com.arassec.artivact.core.model.exchange;

import com.arassec.artivact.core.model.BaseRestrictedObject;
import com.arassec.artivact.core.model.TranslatableString;
import lombok.*;

/**
 * Models a content export i.e., an export designed for exchange of pages and items with other Artivact instances or apps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CollectionExport extends BaseRestrictedObject {

    /**
     * Schema version for versioning exchange formats.
     */
    private static final int SCHEMA_VERSION = 1;

    /**
     * The export's title.
     */
    private TranslatableString title;

    /**
     * The export's short description.
     */
    private TranslatableString description;

    /**
     * A longer description of the export's content.
     */
    private TranslatableString content;

    /**
     * The configuration used to create this content export.
     */
    private ExportConfiguration exportConfiguration;

    /**
     * The type of the exchanged data.
     */
    private ContentSource contentSource;

    /**
     * The source's ID of the exchanged content.
     */
    private String sourceId;

    /**
     * Timestamp of the export file's last modification.
     */
    private long fileLastModified;

    /**
     * The size of the export file.
     */
    private long fileSize;

    /**
     * Indicates whether the file exists in the file repository or not.
     */
    private boolean filePresent;

    /**
     * File extension of the cover picture (if present).
     */
    private String coverPictureExtension;

    /**
     * Marks this collection export as not re-creatable i.e., only the export file is available for distribution.
     */
    private boolean distributionOnly;

}
