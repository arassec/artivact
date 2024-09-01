package com.arassec.artivact.persistence.filesystem;

import com.arassec.artivact.core.misc.FileModification;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link FilesystemFileRepository}.
 */
@ExtendWith(MockitoExtension.class)
public class FilesystemFileRepositoryTest {

    /**
     * Directory to use during tests.
     */
    private final Path testDir = Path.of("target/FilesystemFileRepositoryTest");

    /**
     * File to use during tests.
     */
    private final Path testFile = Path.of("src/test/resources/filesystem-file-repository-test.txt.tpl");

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
    public void setUp() {
        if (Files.exists(testDir)) {
            FileUtils.deleteDirectory(testDir.toFile());
        }
        Files.createDirectories(testDir);
    }

    /**
     * Tests updating the project directory in server mode, i.e. not updating anything at all.
     */
    @Test
    @SneakyThrows
    void testUpdateProjectDirectoryServerMode() {
        when(environment.matchesProfiles("desktop")).thenReturn(false);

        filesystemFileRepository.updateProjectDirectory(testDir, null, null, List.of());

        try (Stream<Path> files = Files.list(testDir)) {
            assertTrue(files.toList().isEmpty());
        }
    }

    /**
     * Tests updating the project directory.
     */
    @Test
    @SneakyThrows
    void testUpdateProjectDirectory() {
        when(environment.matchesProfiles("desktop")).thenReturn(true);

        filesystemFileRepository.updateProjectDirectory(testDir,
                testDir.resolve("invalid"),
                Path.of("src/test/resources/"),
                List.of(new FileModification(
                        "filesystem-file-repository-test.txt",
                        "##REPLACEMENT##",
                        "FilesystemFileRepositoryTest"))
        );

        assertTrue(Files.exists(testDir.resolve("filesystem-file-repository-test.txt.tpl")));
        assertEquals("FilesystemFileRepositoryTest", Files.readString(testDir.resolve("filesystem-file-repository-test.txt")));
    }

    /**
     * Tests emptying a directory.
     */
    @Test
    @SneakyThrows
    void testEmptyDir() {
        Path targetFile = testDir.resolve("temp.txt");
        Files.copy(testFile, targetFile);

        filesystemFileRepository.emptyDir(testDir);

        assertFalse(Files.exists(targetFile));
    }

    /**
     * Tests creating a directory if it doesn't exist.
     */
    @Test
    void testCreateDirIfRequired() {
        Path dir = testDir.resolve("dir");
        assertFalse(Files.exists(dir));
        filesystemFileRepository.createDirIfRequired(dir);
        assertTrue(Files.exists(dir));
    }

    /**
     * Tests deleting a file and directory.
     */
    @Test
    @SneakyThrows
    void testDelete() {
        Path file = testDir.resolve("file.txt");
        Files.copy(testFile, file);

        assertTrue(Files.exists(file));
        filesystemFileRepository.delete(file);
        assertFalse(Files.exists(file));

        assertTrue(Files.exists(testDir));
        Files.copy(testFile, file);
        filesystemFileRepository.delete(testDir);
        assertFalse(Files.exists(file));
    }

    /**
     * Tests checking a file for existence.
     */
    @Test
    void testExists() {
        assertTrue(filesystemFileRepository.exists(testDir));
        assertFalse(filesystemFileRepository.exists(testDir.resolve("file.txt")));
    }

    /**
     * Tests copying a file by path.
     */
    @Test
    void testCopyPath() {
        Path file = testDir.resolve("filesystem-file-repository-test.txt.tpl");
        assertFalse(Files.exists(file));

        filesystemFileRepository.copy(testFile, file);
        assertTrue(Files.exists(file));
    }

    /**
     * Tests copying a file by input stream.
     */
    @Test
    void testCopyInputStream() {
        Path file = testDir.resolve("file.txt");
        assertFalse(Files.exists(file));

        filesystemFileRepository.copy(new ByteArrayInputStream(new byte[0]), file);
        assertTrue(Files.exists(file));
    }

    /**
     * Tests copying a file to an output stream.
     */
    @Test
    @SneakyThrows
    void testCopyToOutputStream() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            filesystemFileRepository.copy(testFile, bos);
            assertEquals("##REPLACEMENT##", bos.toString());
        }
    }

    /**
     * Tests getting a directory from an ID.
     */
    @Test
    void testGetDirFromId() {
        Path dir = filesystemFileRepository.getDirFromId(testDir, "123456789");
        assertEquals(Path.of("target/FilesystemFileRepositoryTest/123/456/123456789"), dir);
    }

    /**
     * Tests getting the filepath to a subdirectory for an ID.
     */
    @Test
    void testGetSubdirFilePath() {
        Path dir = filesystemFileRepository.getSubdirFilePath(testDir, "123456789", null);
        assertEquals(Path.of("target/FilesystemFileRepositoryTest/123/456/123456789"), dir);

        dir = filesystemFileRepository.getSubdirFilePath(testDir, "123456789", "subdir");
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
        Path file = testDir.resolve("file.txt");
        Files.copy(testFile, file);

        List<Path> files = filesystemFileRepository.list(testDir);

        assertEquals(1, files.size());
        assertEquals(file, files.getFirst());
    }

    /**
     * Tests getting the time of last modification of a file.
     */
    @Test
    @SneakyThrows
    void testLastModified() {
        Path file = testDir.resolve("file.txt");
        Path createdFile = Files.createFile(file);

        Instant lastModifiedFromRepo = filesystemFileRepository.lastModified(file);

        Instant lastModifiedFromFilesystem = Files.getLastModifiedTime(createdFile).toInstant();

        assertEquals(lastModifiedFromRepo, lastModifiedFromFilesystem);
    }

    /**
     * Tests testing a path if it's a directory or not.
     */
    @Test
    void testIsDir() {
        assertTrue(filesystemFileRepository.isDir(testDir));
        assertFalse(filesystemFileRepository.isDir(testDir.resolve("file.txt")));
        assertFalse(filesystemFileRepository.isDir(null));
    }

    /**
     * Tests writing file contents into a byte array.
     */
    @Test
    @SneakyThrows
    void testWrite() {
        Path file = testDir.resolve("file.txt");
        filesystemFileRepository.write(file, "testWrite()".getBytes());
        assertEquals("testWrite()", Files.readString(file));
    }

}
