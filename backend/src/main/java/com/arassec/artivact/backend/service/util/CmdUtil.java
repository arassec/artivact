package com.arassec.artivact.backend.service.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.Executor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Commandline utility.
 */
@Slf4j
@Component
public class CmdUtil {

    /**
     * Executes a command on the system's command line and waits for the results.
     *
     * @param cmdLine The command to execute.
     * @return {@code true} if the command executed successfully, {@code false} otherwise.
     */
    public boolean execute(CommandLine cmdLine) {
        var resultHandler = new DefaultExecuteResultHandler();

        Executor executor = new DaemonExecutor();
        executor.setExitValue(1);

        try {
            executor.execute(cmdLine, resultHandler);
        } catch (IOException e) {
            log.error("Exception during 'execute' operation!", e);
        }

        try {
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            log.error("Interrupted during 'execute' operation!", e);
            Thread.currentThread().interrupt();
        }

        boolean executionSuccessful = resultHandler.getExitValue() == 0 || resultHandler.getException() == null;

        if (!executionSuccessful) {
            log.error("Exception during command execution!", resultHandler.getException());
        }

        return executionSuccessful;
    }

}
