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

}
