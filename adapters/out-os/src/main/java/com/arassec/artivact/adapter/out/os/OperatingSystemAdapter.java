package com.arassec.artivact.adapter.out.os;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.Executor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Implements the {@link OsGateway} port.
 */
@Slf4j
@Component
public class OperatingSystemAdapter implements OsGateway {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(String command, List<String> arguments) {

        CommandLine cmdLine = new CommandLine(command);
        if (arguments != null && !arguments.isEmpty()) {
            arguments.forEach(cmdLine::addArgument);
        }

        var resultHandler = new DefaultExecuteResultHandler();

        Executor executor = DaemonExecutor.builder().get();
        executor.setExitValue(1);

        log.debug("Executing command: {}", cmdLine);

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
            log.error("Problem during command execution!", resultHandler.getException());
        }

        log.debug("Executed command finished (success={}).", executionSuccessful);
    }

}
