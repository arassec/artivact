package com.arassec.artivact.backend.api.security;

import com.arassec.artivact.backend.api.ConfigurationController;
import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.model.configuration.AppearanceConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ConfigurationController}.
 */
@ExtendWith(MockitoExtension.class)
public class ConfigurationControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private ConfigurationController controller;

    /**
     * Service mock.
     */
    @Mock
    private ConfigurationService configurationService;

    /**
     * Tests getting available locales.
     */
    @Test
    void testGetAvailableLocales() {
        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
        appearanceConfiguration.setAvailableLocales("de,nl");

        when(configurationService.loadAppearanceConfiguration()).thenReturn(appearanceConfiguration);

        List<String> result = controller.getAvailableLocales();
        assertEquals("de", result.get(0));
        assertEquals("nl", result.get(1));
    }

    /**
     * Tests getting the application locale.
     */
    @Test
    void testGetApplicationLocale() {
        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
        appearanceConfiguration.setApplicationLocale("ja");

        when(configurationService.loadAppearanceConfiguration()).thenReturn(appearanceConfiguration);

        assertEquals("ja", controller.getApplicationLocale());
    }

    /**
     * Tests getting available roles.
     */
    @Test
    void testGetAvailableRoles() {
        List<String> roles = controller.getAvailableRoles();
        assertEquals(2, roles.size());
        assertEquals("ROLE_ADMIN", roles.get(0));
        assertEquals("ROLE_USER", roles.get(1));
    }

    /**
     * Tests getting the application's title.
     */
    @Test
    void testGetTitle() {
        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
        appearanceConfiguration.setApplicationTitle("Application Title");

        when(configurationService.loadAppearanceConfiguration()).thenReturn(appearanceConfiguration);

        assertEquals("Application Title", controller.getTitle());
    }

}
