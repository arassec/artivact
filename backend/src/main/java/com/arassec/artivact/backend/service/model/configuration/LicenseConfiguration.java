package com.arassec.artivact.backend.service.model.configuration;

import com.arassec.artivact.backend.service.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for the license of item media files.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LicenseConfiguration {

    /**
     * License prefix text.
     */
    private TranslatableString prefix;

    /**
     * License label.
     */
    private TranslatableString licenseLabel;

    /**
     * License suffix text.
     */
    private TranslatableString suffix;

    /**
     * HTTP-URL to the license.
     */
    private String licenseUrl;

}
