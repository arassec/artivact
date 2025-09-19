package com.arassec.artivact.application.service.project;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.misc.ExchangeDefinitions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectFileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @InjectMocks
    private ProjectFileService service;

    private final Path exportsDir = Path.of("exports");

    @Test
    void testCleanupTagsConfigurationExport() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);
        service.cleanupTagsConfigurationExport();
        verify(fileRepository).delete(exportsDir.resolve(ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON));
    }

    @Test
    void testCleanupPropertiesConfigurationExport() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);
        service.cleanupPropertiesConfigurationExport();
        verify(fileRepository).delete(exportsDir.resolve(ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON));
    }

}
