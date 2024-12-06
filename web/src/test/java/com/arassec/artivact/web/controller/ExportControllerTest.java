package com.arassec.artivact.web.controller;


import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.exchange.StandardExportInfo;
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
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
     * Tests exporting content.
     */
    @Test
    void testExportContent() {
        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        when(exportService.getProgressMonitor()).thenReturn(progressMonitor);

        ExportConfiguration exportConfiguration = ExportConfiguration.builder().build();

        ResponseEntity<OperationProgress> responseEntity = exportController.exportContent("menu-id", exportConfiguration);

        OperationProgress operationProgress = responseEntity.getBody();
        assertNotNull(operationProgress);

        verify(exportService, times(1)).exportContent(exportConfiguration, "menu-id");
    }

    /**
     * Tests loading content exports.
     */
    @Test
    void testLoadContentExports() {
        StandardExportInfo standardExportInfo = StandardExportInfo.builder().build();
        when(exportService.loadContentExports()).thenReturn(List.of(
                standardExportInfo
        ));

        List<StandardExportInfo> standardExportInfos = exportController.loadContentExports();

        assertEquals(1, standardExportInfos.size());
        assertEquals(standardExportInfo, standardExportInfos.getFirst());
    }

    /**
     * Tests loading the content export overviews.
     */
    @Test
    @SneakyThrows
    void testLoadContentExportOverviews() {
        doAnswer((Answer<OutputStream>) invocationOnMock -> {
            OutputStream outputStream = invocationOnMock.getArgument(0, OutputStream.class);
            outputStream.write("test".getBytes());
            return outputStream;
        }).when(exportService).loadContentExportOverviews(any(OutputStream.class));

        HttpServletResponse response = mock(HttpServletResponse.class);

        ResponseEntity<StreamingResponseBody> streamingResponseBodyResponseEntity = exportController.loadContentExportOverviews(response);

        assertThat(streamingResponseBodyResponseEntity.getBody()).isNotNull();

        verify(response, times(1)).setContentType("application/zip");
        verify(response, times(1)).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());

        OutputStream clientOutputStream = new ByteArrayOutputStream();
        streamingResponseBodyResponseEntity.getBody().writeTo(clientOutputStream);
        assertThat(clientOutputStream.toString()).isEqualTo("test");

    }

    /**
     * Tests downloading a content export.
     */
    @Test
    void testDownloadContentExport() {
        when(exportService.getContentExportFile("menu-id")).thenReturn(
                Path.of("content-export.zip")
        );

        HttpServletResponse response = mock(HttpServletResponse.class);

        ResponseEntity<StreamingResponseBody> responseEntity = exportController.downloadContentExport(response, "menu-id");

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

        verify(exportService, times(1)).deleteExport("menu-id");
    }

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
     * Tests exporting an item.
     */
    @Test
    void testExportItem() {
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

        Path pathMock = mock(Path.class);
        when(exportService.exportItem("123-ABC")).thenReturn(pathMock);

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
