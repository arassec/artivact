package com.arassec.artivact.adapter.out.os;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.Executor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
    public boolean execute(String command, List<String> arguments) {

        CommandLine cmdLine = new CommandLine(command);
        if (arguments != null && !arguments.isEmpty()) {
            arguments.forEach(cmdLine::addArgument);
        }

        var resultHandler = new DefaultExecuteResultHandler();

        Executor executor = DaemonExecutor.builder().get();
        executor.setExitValues(new int[]{0, 1});

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

        boolean executionSuccessful = resultHandler.getExitValue() == 0 && resultHandler.getException() == null;

        if (!executionSuccessful) {
            log.error("Problem during command execution!", resultHandler.getException());
        }

        log.debug("Executed command finished (success={}).", executionSuccessful);

        return executionSuccessful;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExecutable(String command) {
        return Files.isExecutable(Path.of(command));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Path> scanForDirectory(Path startDir, int maxDepth, String dirNamePrefix) {

        AtomicReference<Path> result = new AtomicReference<>();

        try {
            Files.walkFileTree(startDir, EnumSet.noneOf(FileVisitOption.class), maxDepth, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.getFileName().toString().startsWith(dirNamePrefix) && !Files.isSymbolicLink(dir)) {
                        result.set(dir);
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("Error during command scanning!", e);
        }

        return Optional.ofNullable(result.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

}
