package com.arassec.artivact.adapter.out.turntable;

import com.arassec.artivact.application.port.out.peripheral.TurntablePeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.adapter.BasePeripheralAdapter;
import com.arassec.artivact.domain.model.adapter.PeripheralAdapterInitParams;
import com.arassec.artivact.domain.model.configuration.AdapterImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The open source Artivact turntable.
 */
@Slf4j
@Component
@Getter
public class ArtivactTurntableAdapter extends BasePeripheralAdapter implements TurntablePeripheral {

    /**
     * Indicates whether the turntable was found using USB or not.
     */
    private final AtomicBoolean turntableFound = new AtomicBoolean(false);

    /**
     * Indicates whether the rotation finished or not.
     */
    private final AtomicBoolean finished = new AtomicBoolean(false);

    /**
     * The USB serial port the turntable is attached to.
     */
    private SerialPort liveSerialPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.ARTIVACT_TURNTABLE_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ProgressMonitor progressMonitor, PeripheralAdapterInitParams initParams) {
        super.initialize(progressMonitor, initParams);

        log.trace("Initialization Artivact-Turntable");

        reset();

        SerialPort[] serialPorts = SerialPort.getCommPorts();

        for (SerialPort p : serialPorts) {
            log.trace("Checking serial port for artivact turntable {} - {} - {}", p.getSystemPortName(),
                    p.getDescriptivePortName(), p.getPortDescription());

            p.setComPortParameters(9600, 8, 1, 0); // default connection settings for Arduino
            p.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0); // block until bytes can be written

            if (p.openPort(500)) {
                log.trace("Serial port open: {}", p.isOpen());

                turntableFound.set(true);

                liveSerialPort = p;

                liveSerialPort.addDataListener(new SerialPortDataListener() {
                    @Override
                    public void serialEvent(SerialPortEvent event) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(250);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new ArtivactException("Interrupted during turntable interaction!", e);
                        }
                        var resultBuilder = new StringBuilder();

                        int size = event.getSerialPort().bytesAvailable();

                        log.trace("About to read {} bytes from turntable.", size);

                        var buffer = new byte[size];
                        event.getSerialPort().readBytes(buffer, size);
                        resultBuilder.append(new String(buffer));

                        String commandResult = resultBuilder.toString().trim();

                        // "d" as in "done"...
                        if (commandResult.startsWith("d")) {
                            log.trace("Finished rotating artivact turntable.");
                            finished.set(true);
                        }
                    }

                    @Override
                    public int getListeningEvents() {
                        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                    }
                });
            }
        }

        if (!turntableFound.get()) {
            log.info("No active turntable found on any serial port!");
        }

        log.trace("Turntable initialization finished.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void rotate(int numPhotos, int turntableDelay) {

        try {
            if (turntableDelay > 0) {
                TimeUnit.MILLISECONDS.sleep(turntableDelay);
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new ArtivactException("Error during turntable usage!", e);
        }

        if (!turntableFound.get()) {
            return;
        }

        finished.set(false);

        try {
            liveSerialPort.getOutputStream().write(("t" + numPhotos + "\n").getBytes());
            liveSerialPort.getOutputStream().flush();

            while (!finished.get() && turntableFound.get()) {
                TimeUnit.MILLISECONDS.sleep(250);
                log.trace("Waiting for turntable to finish...");
                log.trace("Current status: {} / {}", finished.get(), turntableFound.get());
            }

            log.trace("Turntable finished.");
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new ArtivactException("Error during turntable usage!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void teardown() {
        super.teardown();
        reset();
    }

    /**
     * Resets the serial port and turntable properties.
     */
    private void reset() {
        log.trace("Resetting Artivact-Turntable!");
        if (liveSerialPort != null && !liveSerialPort.closePort()) {
            throw new IllegalStateException("Could not close serial port to turntable!");
        }
        liveSerialPort = null;
        turntableFound.set(false);
        finished.set(false);
    }

}
