package com.arassec.artivact.backend.service.model.configuration;

import com.arassec.artivact.backend.service.model.appearance.ColorTheme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppearanceConfiguration {

    private String applicationTitle;

    private String availableLocales;

    private ColorTheme colorTheme;

    /**
     * The 16x16 pixel favicon, as Base64 encoded string.
     */
    private String encodedFaviconSmall;

    /**
     * The 32x32 pixel favicon, as Base64 encoded string.
     */
    private String encodedFaviconLarge;

}
