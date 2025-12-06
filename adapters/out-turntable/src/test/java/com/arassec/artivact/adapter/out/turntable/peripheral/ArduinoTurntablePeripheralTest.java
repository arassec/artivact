package com.arassec.artivact.adapter.out.turntable.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.configs.ArduinoTurntablePeripheralConfig;
import com.fazecast.jSerialComm.SerialPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ArduinoTurntablePeripheralTest {

    private ArduinoTurntablePeripheral peripheral;

    @BeforeEach
    void setUp() {
        OsGateway osGateway = mock(OsGateway.class);
        peripheral = new ArduinoTurntablePeripheral(osGateway);
    }

    @Test
    void shouldInitializeTurntable() {
        // Arrange
        ArduinoTurntablePeripheralConfig config = new ArduinoTurntablePeripheralConfig();
        config.setDelayInMilliseconds(500);

        PeripheralInitParams params = mock(PeripheralInitParams.class);
        when(params.getConfig()).thenReturn(config);

        SerialPort mockPort = mock(SerialPort.class);
        when(mockPort.openPort()).thenReturn(true);
        when(mockPort.getInputStream()).thenReturn(new ByteArrayInputStream("ARTIVACT_TT_V2\n".getBytes()));
        when(mockPort.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        try (MockedStatic<SerialPort> mocked = mockStatic(SerialPort.class)) {
            mocked.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{mockPort});

            // Act
            peripheral.initialize(mock(ProgressMonitor.class), params);
        }

        // Assert
        assertThat(peripheral.getTurntableDelay()).isEqualTo(500);
        assertThat(peripheral.getPort()).isNotNull();
    }

    @Test
    void shouldRotateWithoutPortAndRespectDelay() {
        // Arrange
        var config = new ArduinoTurntablePeripheralConfig();
        config.setDelayInMilliseconds(50);

        PeripheralInitParams params = mock(PeripheralInitParams.class);
        when(params.getConfig()).thenReturn(config);

        // initialize without real port because no Artivact turntable is detected
        try (MockedStatic<SerialPort> mocked = mockStatic(SerialPort.class)) {
            mocked.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{});
            peripheral.initialize(mock(ProgressMonitor.class), params);
        }

        // Act
        long start = System.currentTimeMillis();
        peripheral.rotate(5);
        long duration = System.currentTimeMillis() - start;

        // Assert
        assertThat(duration).isGreaterThanOrEqualTo(50);
    }

    @Test
    void shouldRotateWithPort() {
        // Arrange
        SerialPort mockPort = mock(SerialPort.class);
        when(mockPort.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockPort.getInputStream()).thenReturn(new ByteArrayInputStream("OK\n".getBytes()));
        ReflectionTestUtils.setField(peripheral, "port", mockPort);

        // Act
        peripheral.rotate(10);

        // Assert
        assertThat(peripheral.getPort()).isNotNull();
        verify(mockPort, atLeastOnce()).getOutputStream();
    }

    @Test
    void shouldTeardownCorrectly() {
        // Arrange
        SerialPort mockPort = mock(SerialPort.class);
        when(mockPort.isOpen()).thenReturn(true);
        ReflectionTestUtils.setField(peripheral, "port", mockPort);

        // Act
        peripheral.teardown();

        // Assert
        verify(mockPort).closePort();
        assertThat(peripheral.getPort()).isNull();
    }

    @Test
    void shouldReturnDisconnectedStatusIfNoTurntableFound() {
        // Arrange
        try (MockedStatic<SerialPort> mocked = mockStatic(SerialPort.class)) {
            mocked.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{});

            // Act
            var status = peripheral.getStatus(new ArduinoTurntablePeripheralConfig());

            // Assert
            assertThat(status).isEqualTo(com.arassec.artivact.domain.model.peripheral.PeripheralStatus.DISCONNECTED);
        }
    }

    @Test
    void shouldScanAndFindTurntable() {
        // Arrange
        SerialPort mockPort = mock(SerialPort.class);
        when(mockPort.openPort()).thenReturn(true);
        when(mockPort.getInputStream()).thenReturn(new ByteArrayInputStream("ARTIVACT_TT_V2\n".getBytes()));
        when(mockPort.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        try (MockedStatic<SerialPort> mocked = mockStatic(SerialPort.class)) {
            mocked.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{mockPort});

            // Act
            var result = peripheral.scanPeripherals();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getLabel()).isEqualTo("Arduino Turntable");
        }
    }

}
