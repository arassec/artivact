package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.ImportService;
import com.arassec.artivact.backend.service.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.backend.service.model.configuration.TagsConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ImportController}.
 */
@ExtendWith(MockitoExtension.class)
class ImportControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private ImportController importController;

    /**
     * The application's {@link ConfigurationService}.
     */
    @Mock
    private ConfigurationService configurationService;

    /**
     * The application's {@link ImportService}.
     */
    @Mock
    private ImportService importService;

    /**
     * The object mapper for exports.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Tests importing previously exported properties.
     */
    @Test
    @SneakyThrows
    void testImportPropertiesConfiguration() {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        when(objectMapper.readValue("properties-config-json", PropertiesConfiguration.class)).thenReturn(propertiesConfiguration);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn("properties-config-json".getBytes());

        ResponseEntity<String> stringResponseEntity = importController.importPropertiesConfiguration(file);

        assertEquals("Properties imported.", stringResponseEntity.getBody());
        verify(configurationService, times(1)).savePropertiesConfiguration(propertiesConfiguration);
    }

    /**
     * Tests importing previously exported tags.
     */
    @Test
    @SneakyThrows
    void testImportTagsConfiguration() {
        TagsConfiguration tagsConfiguration = new TagsConfiguration();
        when(objectMapper.readValue("tags-config-json", TagsConfiguration.class)).thenReturn(tagsConfiguration);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn("tags-config-json".getBytes());

        ResponseEntity<String> stringResponseEntity = importController.importTagsConfiguration(file);

        assertEquals("Tags imported.", stringResponseEntity.getBody());
        verify(configurationService, times(1)).saveTagsConfiguration(tagsConfiguration);
    }

    /**
     * Tests item import.
     */
    @Test
    void testImportItem() {
        MultipartFile file = mock(MultipartFile.class);
        ResponseEntity<String> stringResponseEntity = importController.importItem(file);

        assertEquals("Item imported.", stringResponseEntity.getBody());
        verify(importService, times(1)).importItem(file, null);
    }

    /**
     * Tests item import from the local filesystem.
     */
    @Test
    void testImportItems() {
        ResponseEntity<String> stringResponseEntity = importController.importItems();

        assertEquals("Items scanned.", stringResponseEntity.getBody());
        verify(importService, times(1)).importItemsFromFilesystem();
    }

    /**
     * Tests syncing an item.
     */
    @Test
    void testSyncItem() {
        MultipartFile file = mock(MultipartFile.class);
        ResponseEntity<String> stringResponseEntity = importController.syncItem(file, "api-token");

        assertEquals("Item synchronized.", stringResponseEntity.getBody());
        verify(importService, times(1)).importItem(file, "api-token");
    }

}
