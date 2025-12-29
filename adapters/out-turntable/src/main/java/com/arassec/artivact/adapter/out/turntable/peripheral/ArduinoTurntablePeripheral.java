package com.arassec.artivact.adapter.out.turntable.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.peripheral.TurntablePeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.BasePeripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.ArduinoTurntablePeripheralConfig;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An Arduino controlled turntable peripheral using the firmata protocol.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class ArduinoTurntablePeripheral extends BasePeripheral implements TurntablePeripheral {

    /**
     * Gateway for OS interactions.
     */
    private final OsGateway osGateway;

    /**
     * The port the turntable is connected to using USB.
     */
    private SerialPort turntablePort;

    /**
     * Delay after rotating the turntable. Can be used to stop item movement after rotation or to manually turn a
     * turntable between image capturing.
     */
    private long turntableDelay;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void initialize(ProgressMonitor progressMonitor, PeripheralInitParams initParams) {
        super.initialize(progressMonitor, initParams);

        log.trace("Initialize default turntable");

        turntableDelay = ((ArduinoTurntablePeripheralConfig) initParams.getConfig()).getDelayInMilliseconds();

        // Prevent problems when an exception is thrown after the turntable has been initialized.
        stopTurntableIfPresent(turntablePort);

        SerialPort[] serialPorts = SerialPort.getCommPorts();
        for (SerialPort serialPort : serialPorts) {
            if (isArtivactTurntable(serialPort)) {
                this.turntablePort = serialPort;
                break;
            }
        }

        log.trace("Turntable initialization finished.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void rotate(int numPhotos) {
        if (turntablePort == null) {
            log.warn("Turntable not initialized!");
            if (turntableDelay > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(turntableDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Interrupted during turntable delay!", e);
                }
            }
        } else if (!"OK".equals(move(turntablePort, numPhotos))) {
            log.error("Error during turntable move command!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void teardown() {
        stopTurntableIfPresent(turntablePort);
        turntablePort = null;
        super.teardown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralStatus getStatus(PeripheralConfig peripheralConfig) {
        if (inUse.get()) {
            return PeripheralStatus.AVAILABLE;
        }

        stopTurntableIfPresent(turntablePort);

        SerialPort[] serialPorts = SerialPort.getCommPorts();
        for (SerialPort serialPort : serialPorts) {
            if (isArtivactTurntable(serialPort)) {
                stopTurntableIfPresent(serialPort);
                return PeripheralStatus.AVAILABLE;
            }
        }

        return PeripheralStatus.DISCONNECTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PeripheralConfig> scanPeripherals() {
        if (inUse.get()) {
            return List.of();
        }

        SerialPort[] serialPorts = SerialPort.getCommPorts();
        for (SerialPort serialPort : serialPorts) {
            if (isArtivactTurntable(serialPort)) {
                stopTurntableIfPresent(serialPort);
                ArduinoTurntablePeripheralConfig arduinoTurntablePeripheralConfig = new ArduinoTurntablePeripheralConfig();
                arduinoTurntablePeripheralConfig.setPeripheralImplementation(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL);
                arduinoTurntablePeripheralConfig.setLabel("Arduino Turntable");
                arduinoTurntablePeripheralConfig.setFavourite(true);
                arduinoTurntablePeripheralConfig.setDelayInMilliseconds(0);
                return List.of(arduinoTurntablePeripheralConfig);
            }
        }

        return List.of();
    }

    /**
     * Stops the turntable if it is present.
     *
     * @param port The serial port the turntable is connected to.
     */
    private void stopTurntableIfPresent(SerialPort port) {
        if (port != null && port.isOpen()) {
            port.closePort();
        }
    }

    /**
     * Checks if an Artivact turntable is connected to the given port.
     *
     * @param port The serial port to check.
     * @return {@code true} if an Artivact turntable is connected to the port, {@code false} otherwise.
     */
    private boolean isArtivactTurntable(SerialPort port) {
        port.setBaudRate(115200);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, Duration.ofMinutes(1).toMillisPart(), Duration.ofMinutes(1).toMillisPart());

        if (!port.openPort()) {
            throw new ArtivactException("Could not open turntable port: " + port.getSystemPortName());
        }

        try {
            Thread.sleep(1000);
            while (port.bytesAvailable() > 0) {
                //noinspection ResultOfMethodCallIgnored - clean input buffer
                port.getInputStream().read();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactException("Interrupted during turntable port initialization!", e);
        } catch (IOException e) {
            throw new ArtivactException("Could not read turntable port!", e);
        }

        return "ARTIVACT_TT_V2".equals(getVersion(port));
    }

    /**
     * Returns the turntable version connected to the given port.
     *
     * @param port The serial port the turntable is connected to.
     * @return The turntable version.
     */
    private String getVersion(SerialPort port) {
        try {
            writeLine(port, "VERSION");
            /**
             * Reads line.
             *
             * @return The result.
             */
            return readLine(port);
        } catch (IOException e) {
            log.debug("Error during getting turntable version on port {}: {}", port.getSystemPortName(), e.getMessage());
            return null;
        }
    }

    /**
     * Moves the turntable by the given value.
     *
     * @param port  The serial port the turntable is connected to.
     * @param value The move value.
     * @return The turntable response.
     */
    private String move(SerialPort port, int value) {
        try {
            writeLine(port, "MOVE " + value);
            /**
             * Reads line.
             *
             * @return The result.
             */
            return readLine(port);
        } catch (IOException e) {
            throw new ArtivactException("Error during moving turntable on port " + port.getSystemPortName(), e);
        }
    }

    /**
     * Writes a line to the given port.
     *
     * @param port The serial port.
     * @param s    The string to write.
     * @throws IOException If an I/O error occurs.
     */
    private void writeLine(SerialPort port, String s) throws IOException {
        String msg = s + "\n";
        port.getOutputStream().write(msg.getBytes(StandardCharsets.UTF_8));
        port.getOutputStream().flush();
    }

    /**
     * Reads a line from the given port.
     *
     * @param port The serial port.
     * @return The read line.
     * @throws IOException If an I/O error occurs.
     */
    private String readLine(SerialPort port) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = port.getInputStream().read()) != -1) {
            if ((b == '\n' || b == '\r') && !sb.isEmpty()) {
                break;
            }
            sb.append((char) b);
        }
        return sb.toString().trim();
    }

}
