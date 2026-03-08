package com.arassec.artivact.application.service.maintenance;

import com.arassec.artivact.application.port.in.configuration.*;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.menu.LoadMenuUseCase;
import com.arassec.artivact.application.port.in.menu.SaveMenuUseCase;
import com.arassec.artivact.application.port.in.page.LoadPageContentUseCase;
import com.arassec.artivact.application.port.in.page.SavePageContentUseCase;
import com.arassec.artivact.application.port.in.search.SearchItemsUseCase;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.PageContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Tests the {@link CleanupProjectFilesService}.
 */
@ExtendWith(MockitoExtension.class)
class CleanupProjectFilesServiceTest {

    @InjectMocks
    private CleanupProjectFilesService service;

    @Mock
    private LoadMenuUseCase loadMenuUseCase;

    @Mock
    private SaveMenuUseCase saveMenuUseCase;

    @Mock
    private LoadPageContentUseCase loadPageContentUseCase;

    @Mock
    private SavePageContentUseCase savePageContentUseCase;

    @Mock
    private LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;

    @Mock
    private SavePropertiesConfigurationUseCase savePropertiesConfigurationUseCase;

    @Mock
    private LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;

    @Mock
    private SaveTagsConfigurationUseCase saveTagsConfigurationUseCase;

    @Mock
    private LoadAppearanceConfigurationUseCase loadAppearanceConfigurationUseCase;

    @Mock
    private SaveAppearanceConfigurationUseCase saveAppearanceConfigurationUseCase;

    @Mock
    private SearchItemsUseCase searchItemsUseCase;

    @Mock
    private SaveItemUseCase saveItemUseCase;

    @Test
    void testCleanup() {
        service.cleanup();
        verify(loadMenuUseCase).loadTranslatedRestrictedMenus();
        verify(loadPropertiesConfigurationUseCase).loadPropertiesConfiguration();
        verify(loadTagsConfigurationUseCase).loadTagsConfiguration();
        verify(loadAppearanceConfigurationUseCase).loadTranslatedAppearanceConfiguration();
        verify(searchItemsUseCase).search("*", Integer.MAX_VALUE);
    }

    @Test
    void shouldSaveAllConfigurationsAndItemsDuringCleanup() {
        // Given
        var propertiesConfig = mock(PropertiesConfiguration.class);
        var tagsConfig = mock(TagsConfiguration.class);
        var appearanceConfig = mock(AppearanceConfiguration.class);
        var item = mock(Item.class);

        when(loadMenuUseCase.loadTranslatedRestrictedMenus()).thenReturn(List.of());
        when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()).thenReturn(propertiesConfig);
        when(loadTagsConfigurationUseCase.loadTagsConfiguration()).thenReturn(tagsConfig);
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfig);
        when(searchItemsUseCase.search("*", Integer.MAX_VALUE)).thenReturn(List.of(item));

        // When
        service.cleanup();

        // Then
        verify(savePropertiesConfigurationUseCase).savePropertiesConfiguration(propertiesConfig);
        verify(saveTagsConfigurationUseCase).saveTagsConfiguration(tagsConfig);
        verify(saveAppearanceConfigurationUseCase).saveAppearanceConfiguration(appearanceConfig);
        verify(saveItemUseCase).save(item);
    }

    @Test
    void shouldProcessMenuWithTargetPage() {
        // Given
        var pageContent = mock(PageContent.class);
        when(pageContent.getId()).thenReturn("page-123");

        var menu = new Menu();
        menu.setTargetPageId("page-123");
        menu.setMenuEntries(List.of());

        var roles = Set.of(Roles.ROLE_USER, Roles.ROLE_ADMIN);

        when(loadMenuUseCase.loadTranslatedRestrictedMenus()).thenReturn(List.of(menu));
        when(loadPageContentUseCase.loadPageContent("page-123", roles)).thenReturn(pageContent);
        when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()).thenReturn(mock(PropertiesConfiguration.class));
        when(loadTagsConfigurationUseCase.loadTagsConfiguration()).thenReturn(mock(TagsConfiguration.class));
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(mock(AppearanceConfiguration.class));
        when(searchItemsUseCase.search("*", Integer.MAX_VALUE)).thenReturn(List.of());

        // When
        service.cleanup();

        // Then
        verify(saveMenuUseCase).saveMenu(menu);
        verify(loadPageContentUseCase).loadPageContent("page-123", roles);
        verify(savePageContentUseCase).savePageContent("page-123", roles, pageContent);
    }

    @Test
    void shouldProcessMenuWithoutTargetPage() {
        // Given
        var menu = new Menu();
        menu.setMenuEntries(List.of());

        when(loadMenuUseCase.loadTranslatedRestrictedMenus()).thenReturn(List.of(menu));
        when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()).thenReturn(mock(PropertiesConfiguration.class));
        when(loadTagsConfigurationUseCase.loadTagsConfiguration()).thenReturn(mock(TagsConfiguration.class));
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(mock(AppearanceConfiguration.class));
        when(searchItemsUseCase.search("*", Integer.MAX_VALUE)).thenReturn(List.of());

        // When
        service.cleanup();

        // Then
        verify(saveMenuUseCase).saveMenu(menu);
        verifyNoInteractions(loadPageContentUseCase);
        verifyNoInteractions(savePageContentUseCase);
    }

    @Test
    void shouldProcessNestedMenusRecursively() {
        // Given
        var childPageContent = mock(PageContent.class);
        when(childPageContent.getId()).thenReturn("child-page");

        var childMenu = new Menu();
        childMenu.setTargetPageId("child-page");
        childMenu.setMenuEntries(List.of());

        var parentMenu = new Menu();
        parentMenu.setMenuEntries(List.of(childMenu));

        var roles = Set.of(Roles.ROLE_USER, Roles.ROLE_ADMIN);

        when(loadMenuUseCase.loadTranslatedRestrictedMenus()).thenReturn(List.of(parentMenu));
        when(loadPageContentUseCase.loadPageContent("child-page", roles)).thenReturn(childPageContent);
        when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()).thenReturn(mock(PropertiesConfiguration.class));
        when(loadTagsConfigurationUseCase.loadTagsConfiguration()).thenReturn(mock(TagsConfiguration.class));
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(mock(AppearanceConfiguration.class));
        when(searchItemsUseCase.search("*", Integer.MAX_VALUE)).thenReturn(List.of());

        // When
        service.cleanup();

        // Then
        verify(saveMenuUseCase).saveMenu(parentMenu);
        verify(saveMenuUseCase).saveMenu(childMenu);
        verify(loadPageContentUseCase).loadPageContent("child-page", roles);
        verify(savePageContentUseCase).savePageContent("child-page", roles, childPageContent);
    }
}
