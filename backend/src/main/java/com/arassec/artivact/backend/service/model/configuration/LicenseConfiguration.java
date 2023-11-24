package com.arassec.artivact.backend.service.model.configuration;

import com.arassec.artivact.backend.service.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LicenseConfiguration {

    private TranslatableString prefix;

    private TranslatableString licenseLabel;

    private TranslatableString suffix;

    private String licenseUrl;

}
