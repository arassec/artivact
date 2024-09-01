package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.item.MediaContent;
import com.arassec.artivact.core.model.item.MediaCreationContent;
import com.arassec.artivact.core.model.property.Property;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.core.model.tag.Tag;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.core.repository.ItemRepository;
import com.arassec.artivact.domain.aspect.GenerateIdsAspect;
import com.arassec.artivact.domain.aspect.RestrictResultAspect;
import com.arassec.artivact.domain.aspect.TranslateResultAspect;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Import(AnnotationAwareAspectJAutoProxyCreator.class)
@SpringBootTest(classes = {
        ItemService.class,
        GenerateIdsAspect.class,
        RestrictResultAspect.class,
        TranslateResultAspect.class,
        ObjectMapper.class
})
class ItemServiceIntegrationTest {

    /**
     * The service being integration tested.
     */
    @Autowired
    private ItemService itemService;

    /**
     * Repository for item entities.
     */
    @MockBean
    private ItemRepository itemRepository;

    /**
     * Service for configuration handling.
     */
    @MockBean
    private ConfigurationService configurationService;

    /**
     * Service for search management.
     */
    @MockBean
    private SearchService searchService;

    /**
     * The application's {@link FileRepository}.
     */
    @MockBean
    private FileRepository fileRepository;

    /**
     * The project data provider.
     */
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private ProjectDataProvider projectDataProvider;

    /**
     * Mock for the items-directory.
     */
    @MockBean
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
        when(projectDataProvider.getProjectRoot().resolve(anyString())).thenReturn(itemsDir);

        LocaleContextHolder.setLocale(Locale.GERMAN);

        testTag = new Tag();
        testTag.setId("abc");
        testTag.setValue("testTag");
        testTag.setTranslations(Map.of("de", "Test-Tag"));

        testProperty = new Property();
        testProperty.setId("def");
        testProperty.setValue("testProperty");
        testProperty.setTranslations(Map.of("de", "Test-Property"));

        when(configurationService.loadTagsConfiguration()).thenReturn(new TagsConfiguration(List.of(testTag)));

        when(configurationService.loadPropertiesConfiguration()).thenReturn(new PropertiesConfiguration(List.of(
                new PropertyCategory(List.of(testProperty))
        )));
    }

    /**
     * Tests loading an item with translations being applied.
     */
    @Test
    void testLoadTranslated() {
        when(itemRepository.findById("123")).thenReturn(Optional.of(getTestItem(Set.of())));

        Item item = itemService.loadTranslated("123");

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

        Item item = itemService.loadTranslatedRestricted("123");

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

        Item item = itemService.loadTranslatedRestricted("123");

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

        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(mock(Path.class));

        when(itemRepository.save(any(Item.class))).thenReturn(item);

        assertNull(item.getId());

        Item savedItem = itemService.save(item);

        assertNotNull(savedItem.getId());

        verify(searchService, times(1)).updateIndex(savedItem);
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
