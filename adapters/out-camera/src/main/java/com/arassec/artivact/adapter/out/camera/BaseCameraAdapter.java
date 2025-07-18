package com.arassec.artivact.adapter.out.camera;

import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.domain.model.adapter.BasePeripheralAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.Executor;

import java.io.IOException;

/**
 * Base class for camera adapter implementations.
 */
@Slf4j
public abstract class BaseCameraAdapter extends BasePeripheralAdapter implements CameraPeripheral {

    /**
     * TODO: Move to helper use case?
     * Executes a command on the system's command line and waits for the results.
     *
     * @param cmdLine The command to execute.
     */
    protected void execute(CommandLine cmdLine) {
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
            log.error("Exception during command execution!", resultHandler.getException());
        }

        log.debug("Executed command finished (success={}).", executionSuccessful);
    }

}
