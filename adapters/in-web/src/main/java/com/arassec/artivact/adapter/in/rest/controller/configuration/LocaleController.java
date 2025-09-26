package com.arassec.artivact.adapter.in.rest.controller.configuration;

import com.arassec.artivact.application.port.in.configuration.LoadAppearanceConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.SaveAppearanceConfigurationUseCase;
import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/locale")
@Profile("e2e")
public class LocaleController {

    private final LoadAppearanceConfigurationUseCase loadAppearanceConfigurationUseCase;

    private final SaveAppearanceConfigurationUseCase saveAppearanceConfigurationUseCase;

    @GetMapping("/{locale}")
    public void setApplicationLocale(@PathVariable String locale) {
        AppearanceConfiguration appearanceConfiguration = loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration();
        appearanceConfiguration.setApplicationLocale(locale);
        saveAppearanceConfigurationUseCase.saveAppearanceConfiguration(appearanceConfiguration);
    }

}
