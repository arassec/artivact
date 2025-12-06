package com.arassec.artivact.adapter.out.turntable.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.ArduinoTurntablePeripheralConfig;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import com.fazecast.jSerialComm.SerialPort;
import lombok.SneakyThrows;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ArduinoTurntablePeripheral} with Firmata.
 */
@ExtendWith(MockitoExtension.class)
class ArduinoTurntablePeripheralTest {

    @Mock
    private OsGateway osGateway;

    @Test
    void scanPeripheralsReturnsConfigurationWhenArduinoConnected() {
        SerialPort mockPort = mock(SerialPort.class);
        when(mockPort.getSystemPortName()).thenReturn("COM3");
        when(mockPort.getVendorID()).thenReturn(0x2341);
        when(mockPort.getProductID()).thenReturn(0x0058);

        IODevice mockIODevice = mock(IODevice.class);
        ArduinoTurntablePeripheral peripheral = spy(new ArduinoTurntablePeripheral(osGateway));
        doReturn(Optional.of(mockIODevice)).when(peripheral).hasFirmataInstalled("COM3");

        try (MockedStatic<SerialPort> mockedSerialPort = mockStatic(SerialPort.class)) {
            mockedSerialPort.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{mockPort});

            List<PeripheralConfig> result = peripheral.scanPeripherals();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getLabel()).isEqualTo("Arduino Turntable");
        }
    }

    @Test
    void getStatusReturnsAvailableWhenArduinoConnected() {
        SerialPort mockPort = mock(SerialPort.class);
        when(mockPort.getSystemPortName()).thenReturn("COM3");
        when(mockPort.getVendorID()).thenReturn(0x2341);
        when(mockPort.getProductID()).thenReturn(0x0058);

        IODevice mockIODevice = mock(IODevice.class);
        ArduinoTurntablePeripheral peripheral = spy(new ArduinoTurntablePeripheral(osGateway));
        doReturn(Optional.of(mockIODevice)).when(peripheral).hasFirmataInstalled("COM3");

        try (MockedStatic<SerialPort> mockedSerialPort = mockStatic(SerialPort.class)) {
            mockedSerialPort.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{mockPort});

            var status = peripheral.getStatus(new ArduinoTurntablePeripheralConfig());

            assertThat(status).isEqualTo(PeripheralStatus.AVAILABLE);
        }
    }

    @Test
    void initializeSetsUpTurntableCorrectly() {
        SerialPort mockPort = mock(SerialPort.class);
        when(mockPort.getSystemPortName()).thenReturn("COM3");
        when(mockPort.getVendorID()).thenReturn(0x2341);
        when(mockPort.getProductID()).thenReturn(0x0058);

        IODevice mockIODevice = mock(IODevice.class);
        ArduinoTurntablePeripheral peripheral = spy(new ArduinoTurntablePeripheral(osGateway));
        doReturn(Optional.of(mockIODevice)).when(peripheral).hasFirmataInstalled("COM3");

        try (MockedStatic<SerialPort> mockedSerialPort = mockStatic(SerialPort.class)) {
            mockedSerialPort.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{mockPort});

            ArduinoTurntablePeripheralConfig config = new ArduinoTurntablePeripheralConfig();
            PeripheralInitParams initParams = PeripheralInitParams.builder().config(config).build();

            peripheral.initialize(null, initParams);

            Object ioDevice = ReflectionTestUtils.getField(peripheral, "ioDevice");
            assertThat(ioDevice).isInstanceOf(IODevice.class);
        }
    }

    @Test
    void rotateDoesNothingWhenNotInitialized() {
        ArduinoTurntablePeripheral peripheral = new ArduinoTurntablePeripheral(osGateway);
        ReflectionTestUtils.setField(peripheral, "turntableDelay", 10L);

        long start = System.currentTimeMillis();
        peripheral.rotate(4);
        long duration = System.currentTimeMillis() - start;

        assertThat(duration).isGreaterThanOrEqualTo(10L);
    }

    @Test
    void rotateUsesPinsWhenInitialized() throws IOException {
        IODevice mockIODevice = mock(IODevice.class);
        Pin mockPin = mock(Pin.class);

        when(mockIODevice.getPin(anyInt())).thenReturn(mockPin);

        ArduinoTurntablePeripheral peripheral = new ArduinoTurntablePeripheral(osGateway);
        ReflectionTestUtils.setField(peripheral, "ioDevice", mockIODevice);
        ReflectionTestUtils.setField(peripheral, "turntableDelay", 0L);

        peripheral.rotate(4096 * 6); // ergibt 1 Schritt

        verify(mockIODevice, times(4)).getPin(anyInt());
        verify(mockPin, atLeastOnce()).setMode(Pin.Mode.OUTPUT);
        verify(mockPin, atLeastOnce()).setValue(anyLong());
    }

    @Test
    void rotateWrapsIOExceptionInArtivactException() throws IOException {
        IODevice mockIODevice = mock(IODevice.class);
        Pin mockPin = mock(Pin.class);

        when(mockIODevice.getPin(anyInt())).thenReturn(mockPin);
        doThrow(new IOException("test")).when(mockPin).setValue(anyLong());

        ArduinoTurntablePeripheral peripheral = new ArduinoTurntablePeripheral(osGateway);
        ReflectionTestUtils.setField(peripheral, "ioDevice", mockIODevice);
        ReflectionTestUtils.setField(peripheral, "turntableDelay", 0L);

        assertThatThrownBy(() -> peripheral.rotate(4096 * 6))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Error during turntable rotation");
    }

    @Test
    @SneakyThrows
    void teardownStopsTurntableAndCallsSuper() {
        IODevice mockIODevice = mock(IODevice.class);

        ArduinoTurntablePeripheral peripheral = new ArduinoTurntablePeripheral(osGateway);
        ReflectionTestUtils.setField(peripheral, "ioDevice", mockIODevice);

        peripheral.teardown();

        verify(mockIODevice, times(1)).stop();
        Object ioDeviceAfter = ReflectionTestUtils.getField(peripheral, "ioDevice");
        assertThat(ioDeviceAfter).isNull();
    }

}