package com.arassec.artivact.creator.core.adapter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.Executor;

import java.io.IOException;

@Slf4j
public abstract class BaseAdapter {

    protected void executeCommandLine(CommandLine cmdLine) {
        var resultHandler = new DefaultExecuteResultHandler();

        Executor executor = new DaemonExecutor();
        executor.setExitValue(1);

        try {
            executor.execute(cmdLine, resultHandler);
        } catch (IOException e) {
            log.error("Exception during camera operation!", e);
        }

        // some time later the result handler callback was invoked so we
        // can safely request the exit value
        try {
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            log.error("Interrupted during camera operation!", e);
            Thread.currentThread().interrupt();
        }
    }

}
