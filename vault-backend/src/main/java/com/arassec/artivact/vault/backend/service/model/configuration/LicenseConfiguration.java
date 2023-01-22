package com.arassec.artivact.vault.backend.service.model.configuration;

import com.arassec.artivact.vault.backend.service.model.TranslatableItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LicenseConfiguration {

    private TranslatableItem prefix;

    private TranslatableItem licenseLabel;

    private TranslatableItem suffix;

    private String licenseUrl;

}
