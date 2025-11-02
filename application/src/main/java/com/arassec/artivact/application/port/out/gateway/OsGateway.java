package com.arassec.artivact.application.port.out.gateway;

import java.util.List;

/**
 * Gateway to the operating system.
 */
public interface OsGateway {

    /**
     * Executes a command on the system's command line and waits for the results.
     *
     * @param command   The command to execute.
     * @param arguments The optional command arguments.
     */
    void execute(String command, List<String> arguments);

    /**
     * Returns whether the provided command is executable or not.
     *
     * @param command The command to check.
     * @return {@code true}, if the command can be executed by the current user, {@code false} otherwise.
     */
    boolean isExecutable(String command);

}
