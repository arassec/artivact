package com.arassec.artivact.adapter.out.turntable.peripheral;

import com.arassec.artivact.application.port.out.peripheral.TurntablePeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.BasePeripheralAdapter;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * The open source Artivact turntable.
 */
@Slf4j
@Component
@Getter
public class DefaultTurntablePeripheral extends BasePeripheralAdapter implements TurntablePeripheral {

    /**
     * Arduino pins which are connected to IN1-IN4 on the ULN2003.
     */
    private static final int[] MOTOR_PINS = {8, 9, 10, 11};

    /**
     * Stepping sequence for 28BYJ-48.
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
    private int turntableDelay;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.DEFAULT_TURNTABLE_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ProgressMonitor progressMonitor, PeripheralInitParams initParams) {
        super.initialize(progressMonitor, initParams);

        log.trace("Initialize default turntable");

        String turntableDelayConfiguration = initParams.getConfiguration().getConfigValue(getSupportedImplementation());
        try {
            turntableDelay = Integer.parseInt(turntableDelayConfiguration);
        } catch (NumberFormatException e) {
            log.warn("Invalid turntable delay configuration: {}. Needs to be milliseconds. Using default of 250.", turntableDelayConfiguration);
            turntableDelay = 250;
        }

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
                } catch (InterruptedException | IOException e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    log.debug("Arduino device found, but not accessible with firmata4j.", e);
                }
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

            // 4096 steps are one full rotation with a 28BYJ-48 stepper motor:
            int numSteps = (4096 / numPhotos);
            for (int step = 0; step < numSteps; step++) {
                int[] sequence = STEP_SEQUENCE[step % STEP_SEQUENCE.length];
                for (int i = 0; i < motorPins.length; i++) {
                    motorPins[i].setValue(sequence[i]);
                }
                Thread.sleep(2); // Some time is needed to actually set the pin value on the Arduino!
            }

            // Deactivate all pins:
            for (Pin pin : motorPins) {
                pin.setValue(0);
            }

            if (turntableDelay > 0) {
                TimeUnit.MILLISECONDS.sleep(turntableDelay);
            }

        } catch (InterruptedException | IOException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ArtivactException("Error during turntable rotation!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void teardown() {
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

}
