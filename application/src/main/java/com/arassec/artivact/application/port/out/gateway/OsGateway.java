package com.arassec.artivact.application.port.out.gateway;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Gateway to the operating system.
 */
public interface OsGateway {

    /**
     * Executes a command on the system's command line and waits for the results.
     *
     * @param command   The command to execute.
     * @param arguments The optional command arguments.
     * @return {@code true}, if the command was executed successfully, {@code false} otherwise.
     */
    boolean execute(String command, List<String> arguments);

    /**
     * Returns whether the provided command is executable or not.
     *
     * @param command The command to check.
     * @return {@code true}, if the command can be executed by the current user, {@code false} otherwise.
     */
    boolean isExecutable(String command);

    /**
     * Scans for a directory in the local operating system.
     *
     * @param startDir      The directory to start searching in.
     * @param maxDepth      The maximum depth to search.
     * @param dirNamePrefix The dirNamePrefix to search.
     * @return The path to the dirNamePrefix.
     */
    Optional<Path> scanForDirectory(Path startDir, int maxDepth, String dirNamePrefix);

    /**
     * Returns whether the operating system is Windows.
     *
     * @return {@code true} if the OS is Windows, {@code false} otherwise.
     */
    boolean isWindows();

    /**
     * Returns whether the operating system is Linux.
     *
     * @return {@code true} if the OS is Linux, {@code false} otherwise.
     */
    boolean isLinux();

}
