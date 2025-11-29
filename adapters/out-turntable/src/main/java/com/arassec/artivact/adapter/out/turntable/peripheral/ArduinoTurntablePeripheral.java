package com.arassec.artivact.adapter.out.turntable.peripheral;

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
import lombok.extern.slf4j.Slf4j;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An Arduino controlled turntable peripheral using the firmata protocol.
 */
@Slf4j
@Component
@Getter
public class ArduinoTurntablePeripheral extends BasePeripheral implements TurntablePeripheral {

    /**
     * Pins which are connected to IN1-IN4 on the ULN2003.
     */
    private static final int[] MOTOR_PINS = {8, 9, 10, 11};

    /**
     * Stepping sequence for e.g. 28BYJ-48.
     */
    private static final int[][] STEP_SEQUENCE = {
            {1, 0, 0, 0},
            {1, 1, 0, 0},
            {0, 1, 0, 0},
            {0, 1, 1, 0},
            {0, 0, 1, 0},
            {0, 0, 1, 1},
            {0, 0, 0, 1},
            {1, 0, 0, 1}
    };

    /**
     * The turntable connected to a USB port.
     */
    private IODevice ioDevice;

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
        try {
            if (ioDevice != null) {
                ioDevice.stop();
                ioDevice = null;
            }
        } catch (IOException e) {
            throw new ArtivactException("Error during turntable reset!", e);
        }

        SerialPort[] serialPorts = SerialPort.getCommPorts();

        for (SerialPort port : serialPorts) {
            if (checkArduinoAtPort(port)) {
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
        try {
            if (ioDevice == null) {
                log.warn("Turntable not initialized!");
                if (turntableDelay > 0) {
                    TimeUnit.MILLISECONDS.sleep(turntableDelay);
                }
                return;
            }

            // Set pins as output:
            Pin[] motorPins = new Pin[MOTOR_PINS.length];
            for (int i = 0; i < MOTOR_PINS.length; i++) {
                motorPins[i] = ioDevice.getPin(MOTOR_PINS[i]);
                motorPins[i].setMode(Pin.Mode.OUTPUT);
            }

            // A full rotation of the 28BYJ-48 stepper motor consists of 4096 steps.
            // The gear ratio of the Artivact turntable is 1:6.
            int numSteps = ((4096 * 6) / numPhotos);
            for (int step = 0; step < numSteps; step++) {
                int[] sequence = STEP_SEQUENCE[step % STEP_SEQUENCE.length];
                for (int i = 0; i < motorPins.length; i++) {
                    motorPins[i].setValue(sequence[i]);
                }
                // Some time is needed to actually set the pin value on the Arduino!
                TimeUnit.MILLISECONDS.sleep(5);
            }

            // Deactivate all pins:
            for (Pin pin : motorPins) {
                pin.setValue(0);
            }

            if (turntableDelay > 0) {
                TimeUnit.MILLISECONDS.sleep(turntableDelay);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactException("Interrupted during turntable rotation!", e);
        } catch (IOException e) {
            throw new ArtivactException("Error during turntable rotation!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void teardown() {
        try {
            if (ioDevice != null) {
                ioDevice.stop();
                ioDevice = null;
            }
        } catch (IOException e) {
            throw new ArtivactException("Error during turntable reset!", e);
        }
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

        SerialPort[] serialPorts = SerialPort.getCommPorts();
        for (SerialPort port : serialPorts) {
            if (checkArduinoAtPort(port)) {
                try {
                    ioDevice.stop();
                } catch (IOException e) {
                    log.warn("Error during stopping a turntable!", e);
                    return PeripheralStatus.ERROR;
                }
                ioDevice = null;
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
        for (SerialPort port : serialPorts) {
            if (checkArduinoAtPort(port)) {
                try {
                    ioDevice.stop();
                } catch (IOException e) {
                    log.warn("Error during turntable scan!", e);
                    return List.of();
                }
                ioDevice = null;

                ArduinoTurntablePeripheralConfig arduinoTurntablePeripheralConfig = new ArduinoTurntablePeripheralConfig();
                arduinoTurntablePeripheralConfig.setPeripheralImplementation(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL);
                arduinoTurntablePeripheralConfig.setLabel("Arduino Turntable");
                arduinoTurntablePeripheralConfig.setFavourite(true);
                arduinoTurntablePeripheralConfig.setDelayInMilliseconds(100);

                return List.of(arduinoTurntablePeripheralConfig);
            }
        }

        return List.of();
    }

    /**
     * Checks for a typical arduino signature and that firmata is available on the other side.
     *
     * @param port The port to check.
     * @return {@code true} if an arduino with firmata is listening on the port, {@code false} otherwise.
     */
    private boolean checkArduinoAtPort(SerialPort port) {
        String systemPortName = port.getSystemPortName();

        log.trace("Checking serial port for artivact turntable {} - {} - {}", systemPortName,
                port.getDescriptivePortName(), port.getPortDescription());

        // Check if VID/PID matches Arduino Nano Every:
        if ((port.getVendorID() == 0x2341 || port.getVendorID() == 0x2A03)
                && (port.getProductID() == 0x0058 || port.getProductID() == 0x0059)) {
            try {
                ioDevice = new FirmataDevice("/dev/" + systemPortName);
                ioDevice.start();
                ioDevice.ensureInitializationIsDone();
                return true;
            } catch (InterruptedException | IOException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                if (ioDevice != null) {
                    try {
                        ioDevice.stop();
                    } catch (IOException ex) {
                        log.warn("Error during stopping a turntable!", e);
                    }
                }
                log.debug("Arduino device found, but not accessible with firmata4j.", e);
            }
        }

        return false;
    }

}
