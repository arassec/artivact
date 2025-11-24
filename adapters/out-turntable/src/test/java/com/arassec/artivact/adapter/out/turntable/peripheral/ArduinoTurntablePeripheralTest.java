package com.arassec.artivact.adapter.out.turntable.peripheral;

import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.ArduinoTurntablePeripheralConfig;
import com.fazecast.jSerialComm.SerialPort;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ArduinoTurntablePeripheral}.
 */
@ExtendWith(MockitoExtension.class)
class ArduinoTurntablePeripheralTest {

    /**
     * Peripheral under test.
     */
    private final ArduinoTurntablePeripheral peripheral = new ArduinoTurntablePeripheral();

    /**
     * Holds static mock of SerialPort.
     */
    private MockedStatic<SerialPort> serialPortMock;

    @AfterEach
    void tearDown() {
        if (serialPortMock != null) {
            serialPortMock.close();
        }
    }

    /**
     * Tests getting the supported implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(peripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL);
    }

    /**
     * Tests getStatus when no Arduino is connected.
     */
    @Test
    void testGetStatusNoDevice() {
        serialPortMock = Mockito.mockStatic(SerialPort.class);
        serialPortMock.when(SerialPort::getCommPorts).thenReturn(new SerialPort[0]);

        PeripheralStatus status = peripheral.getStatus(new ArduinoTurntablePeripheralConfig());

        assertThat(status).isEqualTo(PeripheralStatus.DISCONNECTED);
    }

    /**
     * Tests scanPeripherals when no Arduino is connected.
     */
    @Test
    void testScanPeripheralsNoDevice() {
        serialPortMock = Mockito.mockStatic(SerialPort.class);
        serialPortMock.when(SerialPort::getCommPorts).thenReturn(new SerialPort[0]);

        List<?> result = peripheral.scanPeripherals();

        assertThat(result).isEmpty();
    }

    /**
     * Tests teardown does not throw if no device is initialized.
     */
    @Test
    void testTeardownWithoutDevice() {
        assertThatCode(peripheral::teardown).doesNotThrowAnyException();
    }

    /**
     * Tests rotate handles missing initialization gracefully (no IOException, but does not throw).
     */
    @Test
    void testRotateWithoutInitialization() {
        assertThatCode(() -> peripheral.rotate(8)).doesNotThrowAnyException();
    }

    /**
     * Tests rotate wraps IOException in ArtivactException when IODevice is present.
     */
    @Test
    void testRotateWithIOException() throws IOException {
        IODevice ioDevice = mock(IODevice.class);
        // inject mocked IODevice via reflection because field is private
        injectIODevice(ioDevice);

        // setMode will throw IOException to simulate hardware error
        Pin pin = mock(Pin.class);
        doThrow(new IOException("test")).when(pin).setMode(any(Pin.Mode.class));
        when(ioDevice.getPin(anyInt())).thenReturn(pin);

        assertThatCode(() -> peripheral.rotate(8))
                .isInstanceOf(ArtivactException.class);
    }

    /**
     * Helper to inject a mocked IODevice into the peripheral.
     */
    private void injectIODevice(IODevice ioDevice) {
        try {
            var field = ArduinoTurntablePeripheral.class.getDeclaredField("ioDevice");
            field.setAccessible(true);
            field.set(peripheral, ioDevice);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
