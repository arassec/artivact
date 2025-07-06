package com.arassec.artivact.domain.model.appearance;

import com.arassec.artivact.domain.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for the license of item media files.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class License {

    /**
     * License prefix text.
     */
    @Builder.Default
    private TranslatableString prefix = new TranslatableString();

    /**
     * License label.
     */
    @Builder.Default
    private TranslatableString licenseLabel = new TranslatableString();

    /**
     * License suffix text.
     */
    @Builder.Default
    private TranslatableString suffix = new TranslatableString();

    /**
     * HTTP-URL to the license.
     */
    private String licenseUrl;

}
