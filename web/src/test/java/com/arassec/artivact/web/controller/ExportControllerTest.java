package com.arassec.artivact.web.controller;


import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.domain.service.ExportService;
import com.arassec.artivact.web.model.OperationProgress;
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
     * The application's {@link ExportService}.
     */
    @Mock
    private ExportService exportService;

    /**
     * Tests exporting the current properties configuration.
     */
    @Test
    @SneakyThrows
    void testExportPropertiesConfiguration() {
        when(exportService.exportPropertiesConfiguration()).thenReturn("properties-configuration-json");

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
        when(exportService.exportTagsConfiguration()).thenReturn("tags-configuration-json");

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
     * Tests the UI API for syncing items.
     */
    @Test
    void testExportItemToRemoteInstance() {
        exportController.exportItemToRemoteInstance("123-abc");
        verify(exportService, times(1)).exportItemToRemoteInstance("123-abc");
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
