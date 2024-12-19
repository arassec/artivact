package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.domain.service.CollectionExportService;
import com.arassec.artivact.web.model.OperationProgress;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link CollectionExportController}.
 */
@ExtendWith(MockitoExtension.class)
class CollectionExportControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private CollectionExportController controller;

    /**
     * Service mock.
     */
    @Mock
    private CollectionExportService collectionExportService;

    /**
     * Tests loading collection exports.
     */
    @Test
    void testLoadCollectionExports() {
        controller.loadCollectionExports();
        verify(collectionExportService).loadAll();
    }

    /**
     * Tests saving a collection export.
     */
    @Test
    void testSaveCollectionExports() {
        List<CollectionExport> collectionExports = new ArrayList<>();
        when(collectionExportService.loadAll()).thenReturn(collectionExports);

        CollectionExport collectionExport = new CollectionExport();

        List<CollectionExport> savedCollectionExports = controller.save(collectionExport);

        assertThat(savedCollectionExports).isEqualTo(collectionExports);

        verify(collectionExportService).save(collectionExport);
    }

    /**
     * Tests saving the sort order of collection exports.
     */
    @Test
    void testSaveSortOrder() {
        List<CollectionExport> collectionExports = new ArrayList<>();
        when(collectionExportService.loadAll()).thenReturn(collectionExports);

        List<CollectionExport> savedCollectionExports = controller.saveSortOrder(collectionExports);

        assertThat(savedCollectionExports).isEqualTo(collectionExports);

        verify(collectionExportService).saveSortOrder(collectionExports);
    }

    /**
     * Tests deleting a collection export.
     */
    @Test
    void testDelete() {
        List<CollectionExport> collectionExports = new ArrayList<>();
        when(collectionExportService.loadAll()).thenReturn(collectionExports);

        List<CollectionExport> savedCollectionExports = controller.delete("123-abc");
        assertThat(savedCollectionExports).isEqualTo(collectionExports);

        verify(collectionExportService).delete("123-abc");
    }

    /**
     * Tests deleting a collection export without export ID.
     */
    @Test
    void testDeleteFailsafe() {
        List<CollectionExport> collectionExports = new ArrayList<>();
        when(collectionExportService.loadAll()).thenReturn(collectionExports);

        List<CollectionExport> savedCollectionExports = controller.delete(null);
        assertThat(savedCollectionExports).isEqualTo(collectionExports);

        savedCollectionExports = controller.delete("");
        assertThat(savedCollectionExports).isEqualTo(collectionExports);
    }

    /**
     * Tests building a collection export file.
     */
    @Test
    void testBuild() {
        ProgressMonitor progressMonitor = new ProgressMonitor(CollectionExportControllerTest.class, "testBuild");
        when(collectionExportService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> responseEntity = controller.build("123-abc");

        verify(collectionExportService).build("123-abc");

        OperationProgress operationProgress = responseEntity.getBody();
        assertThat(operationProgress).isNotNull();
        assertThat(operationProgress.getKey()).isEqualTo("Progress.CollectionExportControllerTest.testBuild");
    }

    /**
     * Tests downloading the export file.
     */
    @Test
    void testDownloadExportFile() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setFilePresent(true);

        when(collectionExportService.load("123-abc")).thenReturn(collectionExport);

        ResponseEntity<StreamingResponseBody> responseEntity = controller.downloadExportFile("123-abc", null, response);

        assertThat(responseEntity.getBody()).isNotNull();

        verify(response).setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        verify(response).setHeader(eq("Content-Disposition"), anyString());
        verify(response).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(response).addHeader(HttpHeaders.EXPIRES, "0");
    }

}
