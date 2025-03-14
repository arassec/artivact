package com.arassec.artivact.domain.misc;

import com.arassec.artivact.core.misc.FileModification;
import com.arassec.artivact.core.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ProjectDataProvider}.
 */
@ExtendWith(MockitoExtension.class)
class ProjectDataProviderTest {

    /**
     * Mock of the {@link FileRepository}.
     */
    @Mock
    private FileRepository fileRepositoryMock;

    /**
     * Argument captor for file modifications.
     */
    @Captor
    private ArgumentCaptor<List<FileModification>> argumentCaptor;

    /**
     * Tests initializing the project's root dir.
     */
    @Test
    void testInitializeProjectDir() {
        ProjectDataProvider projectDataProvider = new ProjectDataProvider("avdata", fileRepositoryMock);

        projectDataProvider.initializeProjectDir();

        verify(fileRepositoryMock, times(1)).createDirIfRequired(Path.of("avdata/items"));
        verify(fileRepositoryMock, times(1)).createDirIfRequired(Path.of("avdata/exports"));
        verify(fileRepositoryMock, times(1)).createDirIfRequired(Path.of("avdata/temp"));
        verify(fileRepositoryMock, times(1)).createDirIfRequired(Path.of("avdata/widgets"));
        verify(fileRepositoryMock, times(1)).createDirIfRequired(Path.of("avdata/sedata"));


        verify(fileRepositoryMock, times(1)).updateProjectDirectory(
                eq(Path.of("avdata")),
                eq(Path.of("resources/project-setup")),
                eq(Path.of("domain/src/main/resources/project-setup")),
                argumentCaptor.capture()
        );

        List<FileModification> fileModifications = argumentCaptor.getValue();
        assertEquals(3, fileModifications.size());
        assertTrue(fileModifications.get(0).file().endsWith("artivact-metashape-2.1-workflow.xml"));
        assertTrue(fileModifications.get(1).file().endsWith("artivact-metashape-2.2-workflow.xml"));
        assertTrue(fileModifications.get(2).file().endsWith("artivact-meshroom-workflow.mg"));
    }

}
