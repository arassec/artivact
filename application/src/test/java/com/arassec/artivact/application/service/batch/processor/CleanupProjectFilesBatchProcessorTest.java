package com.arassec.artivact.application.service.batch.processor;

import com.arassec.artivact.application.port.in.configuration.*;
import com.arassec.artivact.application.port.in.menu.LoadMenuUseCase;
import com.arassec.artivact.application.port.in.menu.SaveMenuUseCase;
import com.arassec.artivact.application.port.in.page.LoadPageContentUseCase;
import com.arassec.artivact.application.port.in.page.SavePageContentUseCase;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.model.batch.BatchProcessingTask;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CleanupProjectFilesBatchProcessorTest {

    @InjectMocks
    private CleanupProjectFilesBatchProcessor processor;

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

    // --- initialize() ---

    @Test
    void testInitializeSavesAllConfigurations() {
        // Given
        when(loadMenuUseCase.loadTranslatedRestrictedMenus()).thenReturn(List.of());

        PropertiesConfiguration propertiesConfig = new PropertiesConfiguration();
        when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()).thenReturn(propertiesConfig);

        TagsConfiguration tagsConfig = new TagsConfiguration();
        when(loadTagsConfigurationUseCase.loadTagsConfiguration()).thenReturn(tagsConfig);

        AppearanceConfiguration appearanceConfig = new AppearanceConfiguration();
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfig);

        // When
        processor.initialize();

        // Then
        verify(savePropertiesConfigurationUseCase).savePropertiesConfiguration(propertiesConfig);
        verify(saveTagsConfigurationUseCase).saveTagsConfiguration(tagsConfig);
        verify(saveAppearanceConfigurationUseCase).saveAppearanceConfiguration(appearanceConfig);
    }

    @Test
    void testInitializeProcessesMenuWithTargetPage() {
        // Given
        Menu menu = createMenu("menu-1", "page-1", List.of());

        when(loadMenuUseCase.loadTranslatedRestrictedMenus()).thenReturn(List.of(menu));

        PageContent pageContent = new PageContent();
        pageContent.setId("page-1");
        when(loadPageContentUseCase.loadPageContent("page-1", Set.of(Roles.ROLE_ADMIN))).thenReturn(pageContent);

        stubConfigurations();

        // When
        processor.initialize();

        // Then
        verify(saveMenuUseCase).saveMenu(menu);
        verify(loadPageContentUseCase).loadPageContent("page-1", Set.of(Roles.ROLE_ADMIN));
        verify(savePageContentUseCase).savePageContent("page-1", Set.of(Roles.ADMIN), pageContent);
    }

    @Test
    void testInitializeSkipsPageForMenuWithoutTargetPageId() {
        // Given
        Menu menu = createMenu("menu-2", null, List.of());

        when(loadMenuUseCase.loadTranslatedRestrictedMenus()).thenReturn(List.of(menu));
        stubConfigurations();

        // When
        processor.initialize();

        // Then
        verify(saveMenuUseCase).saveMenu(menu);
        verify(loadPageContentUseCase, never()).loadPageContent(any(), any());
        verify(savePageContentUseCase, never()).savePageContent(any(), any(), any());
    }

    @Test
    void testInitializeSkipsPageForMenuWithEmptyTargetPageId() {
        // Given
        Menu menu = createMenu("menu-3", "", List.of());

        when(loadMenuUseCase.loadTranslatedRestrictedMenus()).thenReturn(List.of(menu));
        stubConfigurations();

        // When
        processor.initialize();

        // Then
        verify(saveMenuUseCase).saveMenu(menu);
        verify(loadPageContentUseCase, never()).loadPageContent(any(), any());
        verify(savePageContentUseCase, never()).savePageContent(any(), any(), any());
    }

    @Test
    void testInitializeProcessesNestedMenusRecursively() {
        // Given
        Menu leaf = createMenu("leaf", "page-leaf", List.of());
        Menu mid = createMenu("mid", null, List.of(leaf));
        Menu root = createMenu("root", "page-root", List.of(mid));

        when(loadMenuUseCase.loadTranslatedRestrictedMenus()).thenReturn(List.of(root));

        PageContent rootPage = new PageContent();
        rootPage.setId("page-root");
        when(loadPageContentUseCase.loadPageContent("page-root", Set.of(Roles.ROLE_ADMIN))).thenReturn(rootPage);

        PageContent leafPage = new PageContent();
        leafPage.setId("page-leaf");
        when(loadPageContentUseCase.loadPageContent("page-leaf", Set.of(Roles.ROLE_ADMIN))).thenReturn(leafPage);

        stubConfigurations();

        // When
        processor.initialize();

        // Then
        verify(saveMenuUseCase).saveMenu(root);
        verify(saveMenuUseCase).saveMenu(mid);
        verify(saveMenuUseCase).saveMenu(leaf);

        verify(savePageContentUseCase).savePageContent("page-root", Set.of(Roles.ADMIN), rootPage);
        verify(savePageContentUseCase).savePageContent("page-leaf", Set.of(Roles.ADMIN), leafPage);
        verify(savePageContentUseCase, times(2)).savePageContent(any(), any(), any());
    }

    @Test
    void testInitializeProcessesMultipleTopLevelMenus() {
        // Given
        Menu menu1 = createMenu("menu-a", "page-a", List.of());
        Menu menu2 = createMenu("menu-b", "page-b", List.of());

        when(loadMenuUseCase.loadTranslatedRestrictedMenus()).thenReturn(List.of(menu1, menu2));

        PageContent pageA = new PageContent();
        pageA.setId("page-a");
        when(loadPageContentUseCase.loadPageContent("page-a", Set.of(Roles.ROLE_ADMIN))).thenReturn(pageA);

        PageContent pageB = new PageContent();
        pageB.setId("page-b");
        when(loadPageContentUseCase.loadPageContent("page-b", Set.of(Roles.ROLE_ADMIN))).thenReturn(pageB);

        stubConfigurations();

        // When
        processor.initialize();

        // Then
        verify(saveMenuUseCase).saveMenu(menu1);
        verify(saveMenuUseCase).saveMenu(menu2);
        verify(savePageContentUseCase).savePageContent("page-a", Set.of(Roles.ADMIN), pageA);
        verify(savePageContentUseCase).savePageContent("page-b", Set.of(Roles.ADMIN), pageB);
    }

    // --- process() ---

    @Test
    void testProcessReturnsTrueForCleanupProjectFilesTask() {
        // Given
        BatchProcessingParameters params = new BatchProcessingParameters();
        params.setTask(BatchProcessingTask.CLEANUP_PROJECT_FILES);
        Item item = new Item();

        // When
        boolean result = processor.process(params, item);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testProcessReturnsFalseForDifferentTask() {
        // Given
        BatchProcessingParameters params = new BatchProcessingParameters();
        params.setTask(BatchProcessingTask.UPDATE_SEARCH_INDEX);
        Item item = new Item();

        // When
        boolean result = processor.process(params, item);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testProcessReturnsFalseForNullTask() {
        // Given
        BatchProcessingParameters params = new BatchProcessingParameters();
        params.setTask(null);
        Item item = new Item();

        // When
        boolean result = processor.process(params, item);

        // Then
        assertThat(result).isFalse();
    }

    // --- Helper methods ---

    private Menu createMenu(String id, String targetPageId, List<Menu> menuEntries) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setTargetPageId(targetPageId);
        menu.setMenuEntries(menuEntries);
        return menu;
    }

    private void stubConfigurations() {
        lenient().when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration())
                .thenReturn(new PropertiesConfiguration());
        lenient().when(loadTagsConfigurationUseCase.loadTagsConfiguration())
                .thenReturn(new TagsConfiguration());
        lenient().when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration())
                .thenReturn(new AppearanceConfiguration());
    }
}
