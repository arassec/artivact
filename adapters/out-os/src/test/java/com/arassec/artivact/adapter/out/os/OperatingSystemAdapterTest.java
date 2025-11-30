package com.arassec.artivact.adapter.out.os;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link OperatingSystemAdapter}.
 */
class OperatingSystemAdapterTest {

    /**
     * Tests executing a command on the command line.
     */
    @Test
    void testExecute() {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        assertDoesNotThrow(() -> operatingSystemAdapter.execute("echo", List.of("test")));
    }

    /**
     * Tests executing a non-existing command on the command line.
     */
    @Test
    void testExecuteFailsafe() {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        assertDoesNotThrow(() -> operatingSystemAdapter.execute("invalid-non-existing-command", List.of()));
    }

    @Test
    void executeReturnsTrueForSuccessfulCommand() {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        boolean result = operatingSystemAdapter.execute("echo", List.of("artivact"));
        assertThat(result).isTrue();
    }

    @Test
    void executeReturnsFalseForFailingCommand() {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        boolean result = operatingSystemAdapter.execute("sh", List.of("-c", "exit 1"));
        assertThat(result).isFalse();
    }

    @Test
    void isExecutableReturnsTrueForExistingExecutable() {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        boolean result = operatingSystemAdapter.isExecutable("/bin/sh");
        assertThat(result || !System.getProperty("os.name").toLowerCase().contains("linux")).isTrue();
    }

    @Test
    void isExecutableReturnsFalseForNonExistingPath() {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        boolean result = operatingSystemAdapter.isExecutable("/path/that/does/not/exist/foobar");
        assertThat(result).isFalse();
    }

    @Test
    void scanForDirectoryFindsMatchingDirectoryWithinMaxDepth() throws Exception {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        Path tempDir = Files.createTempDirectory("os-adapter-scan-root-");
        Path targetDir = Files.createDirectory(tempDir.resolve("prefix-targetDir"));

        Optional<Path> result = operatingSystemAdapter.scanForDirectory(tempDir, 5, "prefix-");

        assertTrue(result.isPresent());
        assertThat(targetDir).isEqualTo(result.get());
    }

    @Test
    void scanForDirectoryReturnsEmptyWhenNoMatchingDirectoryExists() throws Exception {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        Path tempDir = Files.createTempDirectory("os-adapter-scan-root-");
        Files.createDirectory(tempDir.resolve("otherDir"));

        Optional<Path> result = operatingSystemAdapter.scanForDirectory(tempDir, 5, "prefix-");

        assertTrue(result.isEmpty());
    }

    @Test
    @SneakyThrows
    void scanForDirectorySkipsSymbolicLinks() throws Exception {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        Path tempDir = Files.createTempDirectory("os-adapter-scan-root-");
        Path realDir = Files.createDirectory(tempDir.resolve("realDir"));
        Path linkDir = tempDir.resolve("prefix-linkDir");

        Files.createSymbolicLink(linkDir, realDir);

        Optional<Path> result = operatingSystemAdapter.scanForDirectory(tempDir, 5, "prefix-");

        assertTrue(result.isEmpty());
    }

    @Test
    void scanForDirectoryRespectsMaxDepth() throws Exception {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        Path tempDir = Files.createTempDirectory("os-adapter-scan-root-");
        Path level1 = Files.createDirectory(tempDir.resolve("level1"));
        Files.createDirectory(level1.resolve("prefix-deepDir"));

        Optional<Path> result = operatingSystemAdapter.scanForDirectory(tempDir, 1, "prefix-");

        assertTrue(result.isEmpty());
    }

    @Test
    void isWindowsAndIsLinuxAreNotBothTrue() {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        boolean isWindows = operatingSystemAdapter.isWindows();
        boolean isLinux = operatingSystemAdapter.isLinux();

        assertThat(isWindows && isLinux).isFalse();
    }

    @Test
    void getUserHomeDirectoryReturnsExistingDirectory() {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        Path homeDir = operatingSystemAdapter.getUserHomeDirectory();

        assertThat(homeDir).isNotNull();
        assertTrue(Files.exists(homeDir));
        assertTrue(Files.isDirectory(homeDir));
    }
}
