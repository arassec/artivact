package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.ImportMenuUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.CollectionExportRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionImportServiceTest {

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ImportTagsConfigurationUseCase importTagsConfigurationUseCase;

    @Mock
    private ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;

    @Mock
    private ImportMenuUseCase importMenuUseCase;

    @Mock
    private CollectionExportRepository collectionExportRepository;

    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private CollectionImportService service;

    private Path testFile;
    private Path exportsDir;
    private Path tempDir;
    private ExchangeMainData exchangeMainData;

    @BeforeEach
    void setUp() {
        testFile = Path.of("/tmp/test.zip");
        exportsDir = Path.of("/tmp/exports");
        tempDir = Path.of("/tmp/temp");
        exchangeMainData = new ExchangeMainData();
        exchangeMainData.setId("id1");
        exchangeMainData.getSourceIds().add("source");
        exchangeMainData.setContentSource(ContentSource.COLLECTION);
        exchangeMainData.setCoverPictureExtension("jpg");
    }

    @Test
    void testImportCollectionTriggersBackgroundExecution() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);
        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        when(jsonMapper.readValue(any(File.class), eq(ExchangeMainData.class))).thenReturn(exchangeMainData);
        when(fileRepository.exists(any())).thenReturn(false);

        service.importCollection(testFile);

        verify(fileRepository).unpack(eq(testFile), any());
        verify(importPropertiesConfigurationUseCase).importPropertiesConfiguration(any(ImportContext.class));
        verify(importTagsConfigurationUseCase).importTagsConfiguration(any(ImportContext.class));
        verify(importMenuUseCase).importMenu(any(), eq("source"), eq(true));
        verify(collectionExportRepository).save(any(CollectionExport.class));
    }

    @Test
    void testImportCollectionForDistributionSkipsContentImports() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);
        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        when(jsonMapper.readValue(any(File.class), eq(ExchangeMainData.class))).thenReturn(exchangeMainData);
        when(fileRepository.exists(any())).thenReturn(false);

        service.importCollectionForDistribution(testFile);

        verify(importPropertiesConfigurationUseCase, never()).importPropertiesConfiguration(any(ImportContext.class));
        verify(importTagsConfigurationUseCase, never()).importTagsConfiguration(any(ImportContext.class));
        verify(importMenuUseCase, never()).importMenu(any(), any(), anyBoolean());
        verify(collectionExportRepository).save(any(CollectionExport.class));
    }

    @Test
    void testImportCollectionThrowsOnUnsupportedContentSource() {
        exchangeMainData.setContentSource(ContentSource.ITEM);
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);
        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);
        when(jsonMapper.readValue(any(File.class), eq(ExchangeMainData.class))).thenReturn(exchangeMainData);

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            ProgressMonitor progressMonitor = new ProgressMonitor("test", "test");
            assertThatThrownBy(() -> backgroundOperation.execute(progressMonitor))
                    .isInstanceOf(ArtivactException.class)
                    .hasMessageContaining("Unsupported content source");
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        service.importCollection(testFile);
    }

    @Test
    void testCreateCollectionExportBuildsExpectedObject() throws Exception {
        var method = CollectionImportService.class.getDeclaredMethod("createCollectionExport", ExchangeMainData.class, boolean.class);
        method.setAccessible(true);
        CollectionExport export = (CollectionExport) method.invoke(null, exchangeMainData, true);

        assertThat(export.getId()).isEqualTo("id1");
        assertThat(export.getSourceId()).isEqualTo("source");
        assertThat(export.getContentSource()).isEqualTo(ContentSource.MENU);
        assertThat(export.isDistributionOnly()).isTrue();
    }

}
