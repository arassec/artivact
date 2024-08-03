package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.item.Item;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private FileRepository FileRepository;

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
    void initialize() {
        ProjectDataProvider projectDataProvider = mock(ProjectDataProvider.class, Mockito.RETURNS_DEEP_STUBS);
        when(projectDataProvider.getProjectRoot().resolve(anyString())).thenReturn(itemsDir);

        itemService = new ItemService(itemRepository, configurationService, searchService, FileRepository, objectMapper, projectDataProvider);

        verify(FileRepository, times(1)).createDirIfRequired(itemsDir);
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

}
