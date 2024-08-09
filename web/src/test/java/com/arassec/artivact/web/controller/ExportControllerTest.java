package com.arassec.artivact.web.controller;


import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.export.ContentExport;
import com.arassec.artivact.domain.export.model.ExportParams;
import com.arassec.artivact.domain.export.model.ExportType;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.arassec.artivact.domain.service.ExportService;
import com.arassec.artivact.web.model.OperationProgress;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ExportController}.
 */
@ExtendWith(MockitoExtension.class)
class ExportControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private ExportController exportController;

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
     * The object mapper for exports.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Tests exporting content.
     */
    @Test
    void testExportContent() {
        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        when(exportService.getProgressMonitor()).thenReturn(progressMonitor);

        ExportParams exportParams = ExportParams.builder().build();

        ResponseEntity<OperationProgress> responseEntity = exportController.exportContent("menu-id", exportParams);

        OperationProgress operationProgress = responseEntity.getBody();
        assertNotNull(operationProgress);

        verify(exportService, times(1)).exportContent(exportParams, "menu-id");
    }

    /**
     * Tests loading content exports.
     */
    @Test
    void testLoadContentExports() {
        ContentExport contentExport = ContentExport.builder().build();
        when(exportService.loadContentExports()).thenReturn(List.of(
                contentExport
        ));

        List<ContentExport> contentExports = exportController.loadContentExports();

        assertEquals(1, contentExports.size());
        assertEquals(contentExport, contentExports.getFirst());
    }

    /**
     * Tests downloading a content export.
     */
    @Test
    void testDownloadContentExport() {
        when(exportService.getContentExportFile("menu-id", ExportType.JSON)).thenReturn(
                Path.of("content-export.zip")
        );

        HttpServletResponse response = mock(HttpServletResponse.class);

        ResponseEntity<StreamingResponseBody> responseEntity = exportController.downloadContentExport(response, "menu-id", ExportType.JSON);

        verify(response, times(1)).setContentType("application/zip");
        verify(response, times(1)).setHeader("Content-Disposition", "attachment; filename=content-export.zip");

        assertNotNull(responseEntity.getBody());
    }

    /**
     * Tests deleting content exports.
     */
    @Test
    void testDeleteContentExport() {
        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        when(exportService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> responseEntity = exportController.deleteContentExport("menu-id");

        OperationProgress operationProgress = responseEntity.getBody();
        assertNotNull(operationProgress);

        verify(exportService, times(1)).deleteContentExport("menu-id");
    }

    /**
     * Tests exporting the current properties configuration.
     */
    @Test
    @SneakyThrows
    void testExportPropertiesConfiguration() {
        var propertiesConfiguration = new PropertiesConfiguration();
        when(configurationService.loadPropertiesConfiguration()).thenReturn(propertiesConfiguration);

        when(objectMapper.writeValueAsString(propertiesConfiguration)).thenReturn("properties-configuration-json");

        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

        ResponseEntity<StreamingResponseBody> streamingResponseBodyResponseEntity =
                exportController.exportPropertiesConfiguration(httpServletResponse);

        assertEquals(HttpStatus.OK, streamingResponseBodyResponseEntity.getStatusCode());

        verify(httpServletResponse, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(httpServletResponse, times(1)).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.EXPIRES, "0");
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
                exportController.exportTagsConfiguration(httpServletResponse);

        assertEquals(HttpStatus.OK, streamingResponseBodyResponseEntity.getStatusCode());

        verify(httpServletResponse, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(httpServletResponse, times(1)).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.EXPIRES, "0");
    }

    /**
     * Tests exporting an item.
     */
    @Test
    void testExportItem() {
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

        ResponseEntity<StreamingResponseBody> streamingResponseBodyResponseEntity =
                exportController.exportItem(httpServletResponse, "123-ABC");

        assertEquals(HttpStatus.OK, streamingResponseBodyResponseEntity.getStatusCode());

        verify(httpServletResponse, times(1)).setContentType("application/zip");
        verify(httpServletResponse, times(1)).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.EXPIRES, "0");

        assertNotNull(streamingResponseBodyResponseEntity.getBody());
    }

    /**
     * Tests the UI API for syncing items.
     */
    @Test
    void testExportItemToRemoteInstance() {
        exportController.exportItemToRemoteInstance("123-abc");
        verify(exportService, times(1)).exportItemToRemoteInstance("123-abc");
    }

    /**
     * Tests exporting all modified items to a remote application instance.
     */
    @Test
    void testExportItemsToRemoteInstance() {
        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        when(exportService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> responseEntity = exportController.exportItemsToRemoteInstance();

        OperationProgress operationProgress = responseEntity.getBody();
        assertNotNull(operationProgress);

        verify(exportService, times(1)).exportItemsToRemoteInstance();
    }

    /**
     * Tests getting the progress of a long-running operation.
     */
    @Test
    void testGetProgress() {
        ProgressMonitor progressMonitor = new ProgressMonitor(ExportControllerTest.class, "test");
        progressMonitor.updateProgress(10, 25);
        progressMonitor.updateProgress("error", new Exception());

        when(exportService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> progress = exportController.getProgress();

        assertNotNull(progress.getBody());
        assertEquals("Progress.ExportControllerTest.error", progress.getBody().getKey());
        assertEquals(10, progress.getBody().getCurrentAmount());
        assertEquals(25, progress.getBody().getTargetAmount());
        assertNotNull(progress.getBody().getError());
    }

}
