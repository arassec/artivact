package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.OperationProgress;
import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.ExportService;
import com.arassec.artivact.backend.service.ImportService;
import com.arassec.artivact.backend.service.misc.ProgressMonitor;
import com.arassec.artivact.backend.service.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.backend.service.model.configuration.TagsConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ExchangeController}.
 */
@ExtendWith(MockitoExtension.class)
class ExchangeControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private ExchangeController exchangeController;

    /**
     * The application's {@link ConfigurationService}.
     */
    @Mock
    private ConfigurationService configurationService;

    /**
     * The application's {@link ExportService}.
     */
    @Mock
    private ExportService exportService;

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
     * Tests exporting the current properties configuration.
     */
    @Test
    @SneakyThrows
    void testExportPropertiesConfiguration() {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        when(configurationService.loadPropertiesConfiguration()).thenReturn(propertiesConfiguration);

        when(objectMapper.writeValueAsString(propertiesConfiguration)).thenReturn("properties-configuration-json");

        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

        ResponseEntity<StreamingResponseBody> streamingResponseBodyResponseEntity =
                exchangeController.exportPropertiesConfiguration(httpServletResponse);

        assertEquals(HttpStatus.OK, streamingResponseBodyResponseEntity.getStatusCode());

        verify(httpServletResponse, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(httpServletResponse, times(1)).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.EXPIRES, "0");
    }

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

        ResponseEntity<String> stringResponseEntity = exchangeController.importPropertiesConfiguration(file);

        assertEquals("Properties imported.", stringResponseEntity.getBody());
        verify(configurationService, times(1)).savePropertiesConfiguration(propertiesConfiguration);
    }

    /**
     * Tests the tag configuration export.
     */
    @Test
    @SneakyThrows
    void testExportTagsConfiguration() {
        TagsConfiguration tagsConfiguration = new TagsConfiguration();
        when(configurationService.loadTranslatedRestrictedTags()).thenReturn(tagsConfiguration);

        when(objectMapper.writeValueAsString(tagsConfiguration)).thenReturn("tags-configuration-json");

        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

        ResponseEntity<StreamingResponseBody> streamingResponseBodyResponseEntity =
                exchangeController.exportTagsConfiguration(httpServletResponse);

        assertEquals(HttpStatus.OK, streamingResponseBodyResponseEntity.getStatusCode());

        verify(httpServletResponse, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(httpServletResponse, times(1)).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.EXPIRES, "0");
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

        ResponseEntity<String> stringResponseEntity = exchangeController.importTagsConfiguration(file);

        assertEquals("Tags imported.", stringResponseEntity.getBody());
        verify(configurationService, times(1)).saveTagsConfiguration(tagsConfiguration);
    }

    /**
     * Tests item export.
     */
    @Test
    void testExportItem() {
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

        ResponseEntity<StreamingResponseBody> streamingResponseBodyResponseEntity =
                exchangeController.exportItem(httpServletResponse, "123-ABC");

        assertEquals(HttpStatus.OK, streamingResponseBodyResponseEntity.getStatusCode());

        verify(httpServletResponse, times(1)).setContentType("application/zip");
        verify(httpServletResponse, times(1)).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.EXPIRES, "0");

        verify(exportService, times(1)).createItemExportFile("123-ABC");
    }

    /**
     * Tests item import.
     */
    @Test
    void testImportItem() {
        MultipartFile file = mock(MultipartFile.class);
        ResponseEntity<String> stringResponseEntity = exchangeController.importItem(file);

        assertEquals("Item imported.", stringResponseEntity.getBody());
        verify(importService, times(1)).importItem(file, null);
    }

    /**
     * Tests item import from the local filesystem.
     */
    @Test
    void testImportItems() {
        ResponseEntity<String> stringResponseEntity = exchangeController.importItems();

        assertEquals("Items scanned.", stringResponseEntity.getBody());
        verify(importService, times(1)).importItemsFromFilesystem();
    }

    /**
     * Tests syncing an item.
     */
    @Test
    void testSyncItem() {
        MultipartFile file = mock(MultipartFile.class);
        ResponseEntity<String> stringResponseEntity = exchangeController.syncItem(file, "api-token");

        assertEquals("Item synchronized.", stringResponseEntity.getBody());
        verify(importService, times(1)).importItem(file, "api-token");
    }

    /**
     * Tests the UI API for syncing items.
     */
    @Test
    void testSyncItemUp() {
        exchangeController.syncItemUp("123-abc");
        verify(exportService, times(1)).syncItemUp("123-abc");
    }

    /**
     * Tests getting the progress of a long-running operation.
     */
    @Test
    void testGetProgress() {
        ProgressMonitor progressMonitor = new ProgressMonitor(ExchangeControllerTest.class, "test");
        progressMonitor.updateProgress(10, 25);
        progressMonitor.updateProgress("error", new Exception());

        when(exportService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> progress = exchangeController.getProgress();

        assertNotNull(progress.getBody());
        assertEquals("Progress.ExchangeControllerTest.error", progress.getBody().getKey());
        assertEquals(10, progress.getBody().getCurrentAmount());
        assertEquals(25, progress.getBody().getTargetAmount());
        assertNotNull(progress.getBody().getError());
    }

}
