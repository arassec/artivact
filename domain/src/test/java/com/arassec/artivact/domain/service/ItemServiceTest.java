package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.property.Property;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.core.model.tag.Tag;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.core.repository.ItemRepository;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ItemService}.
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    /**
     * The service under test.
     */
    private ItemService itemService;

    /**
     * Repository for items.
     */
    @Mock
    private ItemRepository itemRepository;

    /**
     * Service for configuration handling.
     */
    @Mock
    private ConfigurationService configurationService;

    /**
     * Service for search management.
     */
    @Mock
    private SearchService searchService;

    /**
     * The application's {@link FileRepository}.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * The object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Mock for the items-directory.
     */
    @Mock
    private Path itemsDir;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void setUp() {
        ProjectDataProvider projectDataProvider = mock(ProjectDataProvider.class, Mockito.RETURNS_DEEP_STUBS);
        when(projectDataProvider.getProjectRoot().resolve(anyString())).thenReturn(itemsDir);

        itemService = new ItemService(itemRepository, configurationService, searchService, fileRepository, objectMapper, projectDataProvider);

        verify(fileRepository, times(1)).createDirIfRequired(itemsDir);
    }

    /**
     * Tests creating an item with its default settings.
     */
    @Test
    void testCreate() {
        Tag defaultTag = new Tag("url", true);

        when(configurationService.loadTranslatedRestrictedTags()).thenReturn(TagsConfiguration.builder()
                .tags(List.of(defaultTag))
                .build()
        );

        Item item = itemService.create();

        assertEquals(1, item.getTags().size());
        assertEquals(defaultTag, item.getTags().getFirst());

        assertEquals(1, item.getRestrictions().size());
        assertEquals(Roles.ROLE_USER, item.getRestrictions().stream().findFirst().orElseThrow());
    }


    /**
     * Tests loading an item that doesn't exist.
     */
    @Test
    void testLoadNotExisting() {
        assertTrue(itemService.load("invalid").isEmpty());
    }

    /**
     * Tests loading an item.
     */
    @Test
    void testLoad() {
        // Configured tag:
        Tag tag = new Tag();
        tag.setId("tagId");
        tag.setValue("tagValue");

        when(configurationService.loadTagsConfiguration()).thenReturn(TagsConfiguration.builder()
                .tags(List.of(tag))
                .build());

        // Configured property:
        Property property = new Property();
        property.setId("propertyId");
        property.setValue("propertyValue");

        PropertyCategory propertyCategory = new PropertyCategory();
        propertyCategory.getProperties().add(property);

        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.getCategories().add(propertyCategory);

        when(configurationService.loadPropertiesConfiguration()).thenReturn(propertiesConfiguration);

        // Existing item with outdated tags configured and non-existing property:
        Tag oldTag = new Tag();
        oldTag.setId("oldTagId");
        oldTag.setValue("oldTagValue");

        Tag outdatedTag = new Tag();
        outdatedTag.setId("tagId");
        outdatedTag.setValue("outdatedTagValue");

        Item item = new Item();
        item.getTags().add(oldTag);
        item.getTags().add(outdatedTag);

        when(itemRepository.findById("id")).thenReturn(Optional.of(item));

        // Call the service:
        Item loadedItem = itemService.load("id").orElseThrow();

        assertEquals(1, loadedItem.getTags().size());
        assertEquals("tagValue", loadedItem.getTags().getFirst().getValue());
        assertNotNull(loadedItem.getProperties().get("propertyId"));
    }

    /**
     * Tests saving an item with images, that are not configured on the item anymore.
     */
    @Test
    void testSaveWithDanglingImages() {
        Item item = new Item();
        item.setId("id");
        item.getMediaContent().getImages().add("image.jpg");
        item.getMediaContent().getModels().add("model.glb");

        Path resourceDir = Path.of("itemServiceTest"); // not written to...

        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(resourceDir);
        when(fileRepository.list(resourceDir.resolve("images"))).thenReturn(List.of(Path.of("image.jpg"), Path.of("image.png")));
        when(fileRepository.list(resourceDir.resolve("models"))).thenReturn(List.of(Path.of("model2.glb"), Path.of("model.glb")));
        when(fileRepository.exists(any(Path.class))).thenReturn(true);
        when(fileRepository.getSubdirFilePath(any(Path.class), anyString(), anyString())).thenReturn(resourceDir);

        when(itemRepository.save(item)).thenReturn(item);

        Item savedItem = itemService.save(item);

        assertEquals(item, savedItem);

        verify(fileRepository, times(6)).delete(any(Path.class));
        verify(fileRepository, times(1)).delete(Path.of("itemServiceTest/image.png"));
        verify(fileRepository, times(1)).delete(Path.of("itemServiceTest/ORIGINAL-image.png"));
        verify(fileRepository, times(1)).delete(Path.of("itemServiceTest/ITEM_CARD-image.png"));
        verify(fileRepository, times(1)).delete(Path.of("itemServiceTest/DETAIL-image.png"));
        verify(fileRepository, times(1)).delete(Path.of("itemServiceTest/PAGE_TITLE-image.png"));
        verify(fileRepository, times(1)).delete(Path.of("itemServiceTest/model2.glb"));

        verify(searchService, times(1)).updateIndex(item);
    }

    /**
     * Tests getting a list of images that exist in the file store but not in the item configuration.
     */
    @Test
    void testGetDanglingImages() {
        Item item = new Item();
        item.setId("id");
        item.getMediaContent().getImages().add("image.jpg");

        Path resourceDir = Path.of("itemServiceTest"); // not written to...

        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(resourceDir);
        when(fileRepository.list(resourceDir.resolve("images"))).thenReturn(List.of(Path.of("image.jpg"), Path.of("image.png")));
        when(fileRepository.exists(any(Path.class))).thenReturn(true);

        List<String> danglingImages = itemService.getDanglingImages(item);

        assertEquals(1, danglingImages.size());
        assertEquals("image.png", danglingImages.getFirst());
    }

    /**
     * Tests deleting an item.
     */
    @Test
    void testDelete() {
        Path itemDir = Path.of("itemServiceTest/123/456/123456789");
        Path itemDirFirstParent = Path.of("itemServiceTest/123/456");
        Path itemDirSecondParent = Path.of("itemServiceTest/123");

        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(itemDir);

        when(fileRepository.list(itemDirFirstParent)).thenReturn(List.of());
        when(fileRepository.list(itemDirSecondParent)).thenReturn(List.of());

        itemService.delete("id");

        verify(itemRepository, times(1)).deleteById("id");

        verify(fileRepository, times(1)).delete(itemDir);
        verify(fileRepository, times(1)).delete(itemDirFirstParent);
        verify(fileRepository, times(1)).delete(itemDirSecondParent);
    }

}
