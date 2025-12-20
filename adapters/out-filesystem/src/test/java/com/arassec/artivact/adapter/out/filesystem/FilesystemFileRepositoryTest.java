package com.arassec.artivact.adapter.out.filesystem;

import com.arassec.artivact.adapter.out.filesystem.repository.FilesystemFileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link FilesystemFileRepository}.
 */
@ExtendWith(MockitoExtension.class)
class FilesystemFileRepositoryTest {

    /**
     * Directory to use during tests.
     */
    private final Path targetDir = Path.of("target/FilesystemFileRepositoryTest");

    /**
     * File to use during tests.
     */
    private final Path sourceFile = Path.of("src/test/resources/filesystem-file-repository-test.txt");

    /**
     * Image to use during tests.
     */
    private final Path sourceImage = Path.of("src/test/resources/023.png");

    /**
     * Directory to use during tests.
     */
    private final Path sourceDir = Path.of("src/test/resources/test-dir");

    /**
     * Packed Directory to use during tests.
     */
    private final Path sourceDirZip = Path.of("src/test/resources/test-dir.zip");

    /**
     * Repository under test.
     */
    @InjectMocks
    private FilesystemFileRepository filesystemFileRepository;

    /**
     * Spring's {@link Environment}.
     */
    @Mock
    private Environment environment;

    /**
     * Sets up the test environment.
     */
    @BeforeEach
    @SneakyThrows
    void setUp() {
        if (Files.exists(targetDir)) {
            FileUtils.deleteDirectory(targetDir.toFile());
        }
        Files.createDirectories(targetDir);
    }

    /**
     * Tests updating the project directory in server mode, i.e. not updating anything at all.
     */
    @Test
    @SneakyThrows
    void testUpdateProjectDirectoryServerMode() {
        when(environment.matchesProfiles("desktop", "e2e")).thenReturn(false);

        filesystemFileRepository.updateProjectDirectory(targetDir, null, null);

        try (Stream<Path> files = Files.list(targetDir)) {
            assertTrue(files.toList().isEmpty());
        }
    }

    /**
     * Tests updating the project directory.
     */
    @Test
    @SneakyThrows
    void testUpdateProjectDirectory() {
        when(environment.matchesProfiles("desktop", "e2e")).thenReturn(true);

        filesystemFileRepository.updateProjectDirectory(targetDir,
                targetDir.resolve("invalid"),
                Path.of("src/test/resources/")
        );

        assertTrue(Files.exists(targetDir.resolve("filesystem-file-repository-test.txt")));
        assertEquals("test file", Files.readString(targetDir.resolve("filesystem-file-repository-test.txt")));
    }

    /**
     * Tests emptying a directory.
     */
    @Test
    @SneakyThrows
    void testEmptyDir() {
        Path targetFile = targetDir.resolve("temp.txt");
        Files.copy(sourceFile, targetFile);

        filesystemFileRepository.emptyDir(targetDir);

        assertFalse(Files.exists(targetFile));
    }

    /**
     * Tests creating a directory if it doesn't exist.
     */
    @Test
    void testCreateDirIfRequired() {
        Path dir = targetDir.resolve("dir");
        assertFalse(Files.exists(dir));
        filesystemFileRepository.createDirIfRequired(dir);
        assertTrue(Files.exists(dir));
    }

    /**
     * Tests failing when creating a directory if it doesn't exist.
     */
    @Test
    @SneakyThrows
    void testCreateDirIfRequiredFail() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.createDirectories(targetDir)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.createDirIfRequired(targetDir));
        }
    }

    /**
     * Tests deleting a file and directory.
     */
    @Test
    @SneakyThrows
    void testDelete() {
        Path file = targetDir.resolve("file.txt");
        Files.copy(sourceFile, file);

        assertTrue(Files.exists(file));
        filesystemFileRepository.delete(file);
        assertFalse(Files.exists(file));

        assertTrue(Files.exists(targetDir));
        Files.copy(sourceFile, file);
        filesystemFileRepository.delete(targetDir);
        assertFalse(Files.exists(file));
    }

    /**
     * Tests deleting a non-existing file and directory.
     */
    @Test
    @SneakyThrows
    void testDeleteFail() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(targetDir)).thenReturn(true);
            filesMock.when(() -> Files.delete(targetDir)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.delete(targetDir));
        }
    }

    /**
     * Tests opening a directory with the operating system's file manager.
     */
    @Test
    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testOpenDirInOs() {
        try (MockedStatic<Runtime> runtimeMockedStatic = Mockito.mockStatic(Runtime.class)) {
            Runtime runtimeMock = mock(Runtime.class);
            runtimeMockedStatic.when(Runtime::getRuntime).thenReturn(runtimeMock);
            filesystemFileRepository.openDirInOs(targetDir);
            ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
            verify(runtimeMock).exec(captor.capture());
            if (System.getProperty("os.name").contains("Windows")) {
                assertThat(captor.getValue()[0]).isEqualTo("cmd");
                assertThat(captor.getValue()[1]).isEqualTo("/c");
                assertThat(captor.getValue()[2]).isEqualTo("start");
                assertThat(captor.getValue()[3]).isNotNull();
            } else {
                assertThat(captor.getValue()[0]).isEqualTo("xdg-open");
                assertThat(captor.getValue()[1]).endsWith("target/FilesystemFileRepositoryTest");
            }
        }
    }

    /**
     * Tests checking a file for existence.
     */
    @Test
    void testExists() {
        assertTrue(filesystemFileRepository.exists(targetDir));
        assertFalse(filesystemFileRepository.exists(targetDir.resolve("file.txt")));
    }

    /**
     * Tests moving a resource.
     */
    @Test
    @SneakyThrows
    void testMoveResource() {
        Path source = targetDir.resolve("source-move.txt");
        Path target = targetDir.resolve("target-move.txt");
        Files.copy(sourceFile, source);

        assertThat(Files.exists(source)).isTrue();
        assertThat(Files.exists(target)).isFalse();

        filesystemFileRepository.move(source, target);

        assertThat(Files.exists(source)).isFalse();
        assertThat(Files.exists(target)).isTrue();
        assertThat(Files.readString(target)).isEqualTo("test file");
    }

    /**
     * Tests error handling when moving a resource.
     */
    @Test
    void moveResourceFail() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.move(sourceFile, targetDir)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.move(sourceFile, targetDir));
        }
    }

    /**
     * Tests copying a file by path.
     */
    @Test
    void testCopyPath() {
        Path file = targetDir.resolve("filesystem-file-repository-test.txt.tpl");
        assertThat(Files.exists(file)).isFalse();

        filesystemFileRepository.copy(sourceFile, file);
        assertThat(Files.exists(file)).isTrue();

        Path dir = targetDir.resolve("dir");
        assertThat(Files.exists(dir)).isFalse();

        filesystemFileRepository.copy(sourceDir, dir);
        assertThat(Files.exists(dir)).isTrue();
    }

    /**
     * Tests error handling when copying a file by path.
     */
    @Test
    void testCopyPathFail() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(sourceFile)).thenReturn(true);
            filesMock.when(() -> Files.copy(sourceFile, targetDir)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.copy(sourceFile, targetDir));
        }
    }

    /**
     * Tests copying a file by input stream.
     */
    @Test
    void testCopyInputStream() {
        Path file = targetDir.resolve("file.txt");
        assertFalse(Files.exists(file));

        filesystemFileRepository.copy(new ByteArrayInputStream(new byte[0]), file);
        assertTrue(Files.exists(file));
    }

    /**
     * Tests error handling when copying a file by input stream.
     */
    @Test
    void testCopyInputStreamFail() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[0]);
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.copy(sourceStream, targetDir)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.copy(sourceStream, targetDir));
        }
    }

    /**
     * Tests copying a file to an output stream.
     */
    @Test
    @SneakyThrows
    void testCopyToOutputStream() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            filesystemFileRepository.copy(sourceFile, bos);
            assertEquals("test file", bos.toString());
        }
    }

    /**
     * Tests error handling when copying a file to an output stream.
     */
    @Test
    @SneakyThrows
    void testCopyToOutputStreamFail() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.copy(sourceFile, bos)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.copy(sourceFile, bos));
        }
    }

    /**
     * Tests getting a directory from an ID.
     */
    @Test
    void testGetDirFromId() {
        Path dir = filesystemFileRepository.getDirFromId(targetDir, "123456789");
        assertEquals(Path.of("target/FilesystemFileRepositoryTest/123/456/123456789"), dir);
    }

    /**
     * Tests getting the filepath to a subdirectory for an ID.
     */
    @Test
    void testGetSubdirFilePath() {
        Path dir = filesystemFileRepository.getSubdirFilePath(targetDir, "123456789", null);
        assertEquals(Path.of("target/FilesystemFileRepositoryTest/123/456/123456789"), dir);

        dir = filesystemFileRepository.getSubdirFilePath(targetDir, "123456789", "subdir");
        assertEquals(Path.of("target/FilesystemFileRepositoryTest/123/456/123456789/subdir"), dir);
    }

    /**
     * Tests getting subdirectories from an ID.
     */
    @Test
    void testGetSubDir() {
        assertEquals("123", filesystemFileRepository.getSubDir("123456789", 0));
        assertEquals("456", filesystemFileRepository.getSubDir("123456789", 1));
        assertThrows(IllegalArgumentException.class, () -> filesystemFileRepository.getSubDir("123456789", 2));
    }

    /**
     * Tests listing files and directories from a path.
     */
    @Test
    @SneakyThrows
    void testList() {
        Path file = targetDir.resolve("file.txt");
        Files.copy(sourceFile, file);

        List<Path> files = filesystemFileRepository.list(targetDir);

        assertEquals(1, files.size());
        assertEquals(file, files.getFirst());
    }

    /**
     * Tests error handling when listing files and directories from a path.
     */
    @Test
    @SneakyThrows
    @SuppressWarnings("resource")
    void testListFail() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(targetDir)).thenReturn(true);
            filesMock.when(() -> Files.list(targetDir)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.list(targetDir));
        }
    }

    /**
     * Tests listing files and directories from a path that does not exist.
     */
    @Test
    @SneakyThrows
    void testListFailsafe() {
        Path file = targetDir.resolve("non-existent-dir");
        List<Path> files = filesystemFileRepository.list(file);
        assertThat(files).isEmpty();
    }

    /**
     * Tests listing file names, excluding images scaled by Artivact.
     */
    @Test
    void testListNamesWithoutScaledImages() {
        List<String> filenames = filesystemFileRepository.listNamesWithoutScaledImages(sourceDir);
        assertThat(filenames).isNotEmpty().doesNotContain("ITEM_CARD-test-image.png");

        assertThat(filesystemFileRepository.listNamesWithoutScaledImages(Path.of("invalid-non-existing-path"))).isEmpty();
    }

    /**
     * Tests getting the time of last modification of a file.
     */
    @Test
    @SneakyThrows
    void testLastModified() {
        Path file = targetDir.resolve("file.txt");
        Path createdFile = Files.createFile(file);

        Instant lastModifiedFromRepo = filesystemFileRepository.lastModified(file);

        Instant lastModifiedFromFilesystem = Files.getLastModifiedTime(createdFile).toInstant();

        assertEquals(lastModifiedFromRepo, lastModifiedFromFilesystem);
    }

    /**
     * Tests error handling when getting the time of last modification of a file.
     */
    @Test
    @SneakyThrows
    void testLastModifiedFail() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.getLastModifiedTime(sourceFile)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.lastModified(sourceFile));
        }
    }

    /**
     * Tests testing a path if it's a directory or not.
     */
    @Test
    void testIsDir() {
        assertTrue(filesystemFileRepository.isDir(targetDir));
        assertFalse(filesystemFileRepository.isDir(targetDir.resolve("file.txt")));
        assertFalse(filesystemFileRepository.isDir(null));
    }

    /**
     * Tests writing file contents into a byte array.
     */
    @Test
    @SneakyThrows
    void testWrite() {
        Path file = targetDir.resolve("file.txt");
        filesystemFileRepository.write(file, "testWrite()".getBytes());
        assertEquals("testWrite()", Files.readString(file));
    }

    /**
     * Tests error handling when writing file contents into a byte array.
     */
    @Test
    @SneakyThrows
    void testWriteFail() {
        byte[] bytes = new byte[0];
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.write(sourceFile, bytes)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.write(sourceFile, bytes));
        }
    }

    /**
     * Tests scaling an image.
     */
    @Test
    void testScaleImageByPath() {
        Path targetImage = targetDir.resolve("scaled-image-path.png");
        filesystemFileRepository.scaleImage(sourceImage, targetImage, 100);
        assertThat(Files.exists(targetImage)).isTrue();
    }

    /**
     * Tests error handling when scaling an image.
     */
    @Test
    void testScaleImageByPathFail() {
        try (MockedStatic<ImageIO> imageIoMock = Mockito.mockStatic(ImageIO.class)) {
            imageIoMock.when(() -> ImageIO.read(sourceFile.toFile())).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.scaleImage(sourceFile, null, 0));
        }
    }

    /**
     * Tests scaling an image.
     */
    @Test
    @SneakyThrows
    void testScaleImageByInputStream() {
        Path targetImage = targetDir.resolve("scaled-image-stream.png");
        filesystemFileRepository.scaleImage(Files.newInputStream(sourceImage), targetImage, "png", 100);
        assertThat(Files.exists(targetImage)).isTrue();
    }

    /**
     * Tests error handling when scaling an image by input stream.
     */
    @Test
    @SneakyThrows
    void testScaleImageByInputStreamFail() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[0]);
        try (MockedStatic<ImageIO> imageIoMock = Mockito.mockStatic(ImageIO.class)) {
            imageIoMock.when(() -> ImageIO.read(sourceStream)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.scaleImage(sourceStream, null, null, 0));
        }
    }

    /**
     * Tests determining the size of a path.
     */
    @Test
    void testSize() {
        assertThat(filesystemFileRepository.size(null)).isZero();
        assertThat(filesystemFileRepository.size(Path.of("INVALID"))).isZero();
        assertThat(filesystemFileRepository.size(sourceFile)).isEqualTo(9);
        assertThat(filesystemFileRepository.size(sourceImage)).isEqualTo(17613);
    }

    /**
     * Tests error handling when determining the size of a path.
     */
    @Test
    void testSizeFail() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(sourceFile)).thenReturn(true);
            filesMock.when(() -> Files.size(sourceFile)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.size(sourceFile));
        }
    }

    /**
     * Tests packing and unpacking a directory.
     */
    @Test
    @SneakyThrows
    void testPack() {
        Path targetZip = targetDir.resolve("test-dir.zip");
        filesystemFileRepository.pack(sourceDir, targetZip);
        assertThat(Files.exists(targetZip)).isTrue();
    }

    /**
     * Tests unpacking and unpacking a directory.
     */
    @Test
    @SneakyThrows
    void testUnpack() {
        Path target = targetDir.resolve("test-dir-unpacked");
        filesystemFileRepository.unpack(sourceDirZip, target);
        assertThat(Files.exists(target.resolve("empty.txt"))).isTrue();
    }

    /**
     * Tests reading a file as String.
     */
    @Test
    void testRead() {
        assertThat(filesystemFileRepository.read(sourceFile)).isEqualTo("test file");
    }

    /**
     * Tests error handling when reading a file as String.
     */
    @Test
    void testReadFail() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.readString(sourceFile)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.read(sourceFile));
        }
    }

    /**
     * Tests reading a file as InputStream.
     */
    @Test
    @SneakyThrows
    void testReadStream() {
        String sourceFileContent = StreamUtils.copyToString(filesystemFileRepository.readStream(sourceFile), Charset.defaultCharset());
        assertThat(sourceFileContent).isEqualTo("test file");
    }

    /**
     * Tests error handling when reading a file as InputStream.
     */
    @Test
    @SneakyThrows
    @SuppressWarnings("resource")
    void testReadStreamFail() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.newInputStream(sourceFile)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.readStream(sourceFile));
        }
    }

    /**
     * Tests reading a file as byte array.
     */
    @Test
    void testReadBytes() {
        byte[] sourceFileContent = filesystemFileRepository.readBytes(sourceFile);
        assertThat(new String(sourceFileContent)).isEqualTo("test file");
    }

    /**
     * Tests error handling when reading a file as byte array.
     */
    @Test
    void testReadBytesFail() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.readAllBytes(sourceFile)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.readBytes(sourceFile));
        }
    }

    /**
     * Tests getting an asset's name.
     */
    @Test
    void testGetAssetName() {
        assertThat(filesystemFileRepository.getAssetName(23, null)).isEqualTo("023");
        assertThat(filesystemFileRepository.getAssetName(23, "")).isEqualTo("023");
        assertThat(filesystemFileRepository.getAssetName(23, "  ")).isEqualTo("023");
        assertThat(filesystemFileRepository.getAssetName(4, "glb")).isEqualTo("004.glb");
        assertThat(filesystemFileRepository.getAssetName(23, "jpg")).isEqualTo("023.jpg");
        assertThat(filesystemFileRepository.getAssetName(666, "png")).isEqualTo("666.png");
    }

    /**
     * Tests getting the next asset number from a directory.
     */
    @Test
    @SneakyThrows
    void testGetNextAssetNumber() {
        assertThat(filesystemFileRepository.getNextAssetNumber(Path.of("invalid-does-not-exist-path"))).isEqualTo(1);

        Files.copy(sourceImage, targetDir.resolve(sourceImage.getFileName()));
        assertThat(filesystemFileRepository.getNextAssetNumber(targetDir)).isEqualTo(24);
    }


    /**
     * Tests error handling when getting the next asset number from a directory.
     */
    @Test
    @SneakyThrows
    @SuppressWarnings("resource")
    void testGetNextAssetNumberFail() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(targetDir)).thenReturn(true);
            filesMock.when(() -> Files.list(targetDir)).thenThrow(new IOException("test-exception"));
            assertThrows(ArtivactException.class, () -> filesystemFileRepository.getNextAssetNumber(targetDir));
        }
    }

    /**
     * Tests deleting a directory and up to two levels of empty parent directories.
     */
    @Test
    @SneakyThrows
    void testDeleteDirAndEmptyParents() {
        // Both parent directories are deleted:
        Path dirToDelete = targetDir.resolve("one/two/three");
        Files.createDirectories(dirToDelete);
        filesystemFileRepository.deleteDirAndEmptyParents(dirToDelete);
        assertThat(Files.exists(targetDir.resolve("one"))).isFalse();

        // One parent dir is deleted:
        dirToDelete = targetDir.resolve("one/two/three");
        Files.createDirectories(dirToDelete);
        Files.copy(sourceImage, targetDir.resolve("one").resolve(sourceImage.getFileName()));
        filesystemFileRepository.deleteDirAndEmptyParents(dirToDelete);
        assertThat(Files.exists(targetDir.resolve("one").resolve(sourceImage.getFileName()))).isTrue();
        FileUtils.deleteDirectory(targetDir.resolve("one").toFile());

        // No parent dir is deleted:
        dirToDelete = targetDir.resolve("one/two/three");
        Files.createDirectories(dirToDelete);
        Files.copy(sourceImage, targetDir.resolve("one/two").resolve(sourceImage.getFileName()));
        filesystemFileRepository.deleteDirAndEmptyParents(dirToDelete);
        assertThat(Files.exists(targetDir.resolve("one/two").resolve(sourceImage.getFileName()))).isTrue();
    }

}
