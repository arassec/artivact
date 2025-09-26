package com.arassec.artivact.adapter.in.rest.controller.configuration;

import com.arassec.artivact.application.port.in.configuration.LoadAppearanceConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.SaveAppearanceConfigurationUseCase;
import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocaleControllerTest {

    @Mock
    private LoadAppearanceConfigurationUseCase loadAppearanceConfigurationUseCase;

    @Mock
    private SaveAppearanceConfigurationUseCase saveAppearanceConfigurationUseCase;

    @InjectMocks
    private LocaleController localeController;

    @Captor
    private ArgumentCaptor<AppearanceConfiguration> appearanceConfigurationCaptor;

    @Test
    void testSetApplicationLocale() {
        AppearanceConfiguration configuration = new AppearanceConfiguration();
        configuration.setApplicationLocale("en");
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration())
                .thenReturn(configuration);

        String newLocale = "de";

        localeController.setApplicationLocale(newLocale);

        verify(loadAppearanceConfigurationUseCase, times(1))
                .loadTranslatedAppearanceConfiguration();
        verify(saveAppearanceConfigurationUseCase, times(1))
                .saveAppearanceConfiguration(appearanceConfigurationCaptor.capture());

        AppearanceConfiguration savedConfiguration = appearanceConfigurationCaptor.getValue();
        assertThat(savedConfiguration.getApplicationLocale()).isEqualTo(newLocale);
    }

}
