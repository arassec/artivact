package com.arassec.artivact.domain.exchange.model;

import com.arassec.artivact.core.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The file containing the main information about the exchanged data.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExchangeMainData {

    /**
     * Schema version for versioning exchange formats.
     */
    @Builder.Default
    private int schemaVersion = 1;

    /**
     * The title of the content.
     */
    private TranslatableString title;

    /**
     * The description of the content.
     */
    private TranslatableString description;

    /**
     * The type of the exchanged data.
     */
    private ExchangeType exchangeType;

    /**
     * The ID of the source of the exchanged content.
     */
    private String sourceId;

}
