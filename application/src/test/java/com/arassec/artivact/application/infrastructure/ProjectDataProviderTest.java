package com.arassec.artivact.application.infrastructure;

import com.arassec.artivact.domain.model.misc.FileModification;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link ProjectService}.
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
    /*
    @Test
    void testInitializeProjectDir() {
        ProjectService projectDataProvider = new ProjectService("avdata", fileRepositoryMock);

        //projectDataProvider.initializeProjectDir();

        verify(fileRepositoryMock, times(1)).createDirIfRequired(Path.of("avdata/items"));
        verify(fileRepositoryMock, times(1)).createDirIfRequired(Path.of("avdata/exports"));
        verify(fileRepositoryMock, times(1)).createDirIfRequired(Path.of("avdata/temp"));
        verify(fileRepositoryMock, times(1)).createDirIfRequired(Path.of("avdata/widgets"));
        verify(fileRepositoryMock, times(1)).createDirIfRequired(Path.of("avdata/sedata"));


        verify(fileRepositoryMock, times(1)).updateProjectDirectory(
                eq(Path.of("avdata")),
                eq(Path.of("resources/project-setup")),
                eq(Path.of("application/src/main/resources/project-setup")),
                argumentCaptor.capture()
        );

        List<FileModification> fileModifications = argumentCaptor.getValue();
        assertEquals(3, fileModifications.size());
        assertTrue(fileModifications.get(0).file().endsWith("artivact-metashape-2.1-workflow.xml"));
        assertTrue(fileModifications.get(1).file().endsWith("artivact-metashape-2.2-workflow.xml"));
        assertTrue(fileModifications.get(2).file().endsWith("artivact-meshroom-workflow.mg"));
    }
     */

}
