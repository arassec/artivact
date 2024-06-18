package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.ItemEntityRepository;
import com.arassec.artivact.backend.persistence.model.ItemEntity;
import com.arassec.artivact.backend.service.aop.GenerateIdsAspect;
import com.arassec.artivact.backend.service.aop.RestrictResultAspect;
import com.arassec.artivact.backend.service.aop.TranslateResultAspect;
import com.arassec.artivact.backend.service.misc.ProjectDataProvider;
import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.backend.service.model.configuration.TagsConfiguration;
import com.arassec.artivact.backend.service.model.item.Item;
import com.arassec.artivact.backend.service.model.item.MediaContent;
import com.arassec.artivact.backend.service.model.item.MediaCreationContent;
import com.arassec.artivact.backend.service.model.property.Property;
import com.arassec.artivact.backend.service.model.property.PropertyCategory;
import com.arassec.artivact.backend.service.model.tag.Tag;
import com.arassec.artivact.backend.service.util.FileUtil;
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
     * The application's object mapper.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Repository for item entities.
     */
    @MockBean
    private ItemEntityRepository itemEntityRepository;

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
     * The application's {@link FileUtil}.
     */
    @MockBean
    private FileUtil fileUtil;

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
    void initialize() {
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
    void testTranslationsOnLoad() {
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setContentJson(getItemJson(Set.of()));

        when(itemEntityRepository.findById("123")).thenReturn(Optional.of(itemEntity));

        Item item = itemService.loadTranslatedRestricted("123");

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
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setContentJson(getItemJson(Set.of("ROLE_USER")));

        when(itemEntityRepository.findById("123")).thenReturn(Optional.of(itemEntity));

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
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setContentJson(getItemJson(Set.of("ROLE_ADMIN")));

        when(itemEntityRepository.findById("123")).thenReturn(Optional.of(itemEntity));

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

        when(fileUtil.getDirFromId(any(Path.class), anyString())).thenReturn(mock(Path.class));

        when(itemEntityRepository.save(any(ItemEntity.class))).thenReturn(new ItemEntity());

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
    private String getItemJson(Set<String> restrictions) {
        Item item = new Item();
        item.setRestrictions(restrictions);
        item.setTitle(new TranslatableString("Title", null, Map.of("de", "Titel")));
        item.setTags(List.of(testTag));
        item.setProperties(Map.of(testProperty.getId(),
                new TranslatableString(testProperty.getValue(), null, testProperty.getTranslations())));

        return objectMapper.writeValueAsString(item);
    }

}
