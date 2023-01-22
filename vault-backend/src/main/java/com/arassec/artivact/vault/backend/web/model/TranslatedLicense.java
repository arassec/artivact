package com.arassec.artivact.vault.backend.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranslatedLicense {

    private String prefix;

    private String licenseLabel;

    private String suffix;

    private String licenseUrl;

}
