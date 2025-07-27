package com.arassec.artivact.application.service;

import com.arassec.artivact.application.infrastructure.aspect.GenerateIdsAspect;
import com.arassec.artivact.application.infrastructure.aspect.RestrictResultAspect;
import com.arassec.artivact.application.infrastructure.aspect.TranslateResultAspect;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.search.ManageSearchIndexUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.ItemRepository;
import com.arassec.artivact.application.service.item.ManageItemService;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaContent;
import com.arassec.artivact.domain.model.item.MediaCreationContent;
import com.arassec.artivact.domain.model.property.Property;
import com.arassec.artivact.domain.model.property.PropertyCategory;
import com.arassec.artivact.domain.model.tag.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Integration test for the {@link ManageItemService}.
 */
@SuppressWarnings("unused")
@Import(AnnotationAwareAspectJAutoProxyCreator.class)
@SpringBootTest(classes = {
        ManageItemService.class,
        GenerateIdsAspect.class,
        RestrictResultAspect.class,
        TranslateResultAspect.class,
        ObjectMapper.class
})
class ItemServiceIntegrationTest {

    @Autowired
    private LoadItemUseCase loadItemUseCase;

    @Autowired
    private SaveItemUseCase saveItemUseCase;

    /**
     * Repository for item entities.
     */
    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;

    @MockitoBean
    private LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;

    /**
     * Use case for search index management.
     */
    @MockitoBean
    private ManageSearchIndexUseCase manageSearchIndexUseCase;

    /**
     * The application's {@link FileRepository}.
     */
    @MockitoBean
    private FileRepository fileRepository;

    /**
     * The project data provider.
     */
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Mock for the directory containing items.
     */
    @MockitoBean
    private Path itemsDir;

    /**
     * A tag for testing.
     */
    private Tag testTag;

    /**
     * A property for testing.
     */
    private Property testProperty;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void setUp() {
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(itemsDir);

        LocaleContextHolder.setLocale(Locale.GERMAN);

        testTag = new Tag();
        testTag.setId("abc");
        testTag.setValue("testTag");
        testTag.setTranslations(Map.of("de", "Test-Tag"));

        testProperty = new Property();
        testProperty.setId("def");
        testProperty.setValue("testProperty");
        testProperty.setTranslations(Map.of("de", "Test-Property"));

        when(loadTagsConfigurationUseCase.loadTagsConfiguration()).thenReturn(new TagsConfiguration(List.of(testTag)));

        when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()).thenReturn(new PropertiesConfiguration(List.of(
                new PropertyCategory(List.of(testProperty))
        )));
    }

    /**
     * Tests loading an item with translations being applied.
     */
    @Test
    void testLoadTranslated() {
        when(itemRepository.findById("123")).thenReturn(Optional.of(getTestItem(Set.of())));

        Item item = loadItemUseCase.loadTranslated("123");

        assertNotNull(item);
        assertEquals("Titel", item.getTitle().getTranslatedValue());

        assertEquals("Test-Tag", item.getTags().getFirst().getTranslatedValue());

        assertEquals("Test-Property", item.getProperties().get(testProperty.getId()).getTranslatedValue());
    }

    /**
     * Tests loading a restricted item, for which the user has permissions, and that translations are applied.
     */
    @Test
    @WithMockUser("ROLE_USER")
    void testLoadTranslatedRestrictedSuccess() {
        when(itemRepository.findById("123")).thenReturn(Optional.of(getTestItem(Set.of("ROLE_USER"))));

        Item item = loadItemUseCase.loadTranslatedRestricted("123");

        assertNotNull(item);
        assertEquals("Titel", item.getTitle().getTranslatedValue());
    }

    /**
     * Tests loading a restricted item for which the user has NO permissions.
     */
    @Test
    @WithMockUser("ROLE_USER")
    void testLoadTranslatedRestrictedFiltered() {
        when(itemRepository.findById("123")).thenReturn(Optional.of(getTestItem(Set.of("ROLE_ADMIN"))));

        Item item = loadItemUseCase.loadTranslatedRestricted("123");

        assertNull(item);
    }

    /**
     * Tests ID generation on saving an item.
     */
    @Test
    void testIdGenerationOnSave() {
        Item item = new Item();
        item.setMediaContent(new MediaContent());
        item.setMediaCreationContent(new MediaCreationContent());

        when(fileRepository.listNamesWithoutScaledImages(any(Path.class))).thenReturn(List.of());
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(mock(Path.class));

        when(itemRepository.save(any(Item.class))).thenReturn(item);

        assertNull(item.getId());

        Item savedItem = saveItemUseCase.save(item);

        assertNotNull(savedItem.getId());

        verify(manageSearchIndexUseCase, times(1)).updateIndex(savedItem);
    }

    /**
     * Creates a JSON representation of an item with the given restrictions.
     *
     * @param restrictions The restrictions of the item.
     * @return The item's JSON representation.
     */
    @SneakyThrows
    private Item getTestItem(Set<String> restrictions) {
        Item item = new Item();
        item.setRestrictions(restrictions);
        item.setTitle(new TranslatableString("Title", null, Map.of("de", "Titel")));
        item.setTags(List.of(testTag));
        item.setProperties(Map.of(testProperty.getId(),
                new TranslatableString(testProperty.getValue(), null, testProperty.getTranslations())));
        return item;
    }

}
