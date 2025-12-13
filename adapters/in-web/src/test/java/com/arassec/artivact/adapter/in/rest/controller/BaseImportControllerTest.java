package com.arassec.artivact.adapter.in.rest.controller;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseImportController}.
 */
@ExtendWith(MockitoExtension.class)
class BaseImportControllerTest {

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private FileRepository fileRepository;

    @TempDir
    Path tempDir;

    /**
     * Concrete implementation for testing.
     */
    private class TestableBaseImportController extends BaseImportController {

        @Override
        protected UseProjectDirsUseCase getUseProjectDirsUseCase() {
            return useProjectDirsUseCase;
        }

        @Override
        protected FileRepository getFileRepository() {
            return fileRepository;
        }

        public Path testSaveTempFile(MultipartFile multipartFile) {
            return saveTempFile(multipartFile);
        }
    }

    /**
     * Tests saving a temp file with original filename.
     */
    @Test
    void testSaveTempFileWithOriginalFilename() throws IOException {
        TestableBaseImportController controller = new TestableBaseImportController();

        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("test.zip");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        Path result = controller.testSaveTempFile(multipartFile);

        assertThat(result.toString()).contains("upload_test.zip");
        verify(fileRepository).copy(any(InputStream.class), eq(result), any(CopyOption.class));
    }

    /**
     * Tests saving a temp file without extension.
     */
    @Test
    void testSaveTempFileWithoutExtension() throws IOException {
        TestableBaseImportController controller = new TestableBaseImportController();

        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("testfile");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        Path result = controller.testSaveTempFile(multipartFile);

        assertThat(result.toString()).contains("upload_testfile.tmp");
        verify(fileRepository).copy(any(InputStream.class), eq(result), any(CopyOption.class));
    }

    /**
     * Tests saving a temp file with null original filename.
     */
    @Test
    void testSaveTempFileWithNullFilename() throws IOException {
        TestableBaseImportController controller = new TestableBaseImportController();

        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        Path result = controller.testSaveTempFile(multipartFile);

        assertThat(result.toString()).contains("upload_.tmp");
        verify(fileRepository).copy(any(InputStream.class), eq(result), any(CopyOption.class));
    }

    /**
     * Tests that IOException is wrapped in ArtivactException.
     */
    @Test
    void testSaveTempFileThrowsArtivactExceptionOnIOException() throws IOException {
        TestableBaseImportController controller = new TestableBaseImportController();

        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("test.zip");
        when(multipartFile.getInputStream()).thenThrow(new IOException("Test IO Exception"));

        assertThatThrownBy(() -> controller.testSaveTempFile(multipartFile))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not create temporary file!");
    }

}
