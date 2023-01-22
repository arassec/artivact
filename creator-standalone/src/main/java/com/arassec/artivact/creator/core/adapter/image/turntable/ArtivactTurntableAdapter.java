package com.arassec.artivact.creator.core.adapter.image.turntable;

import com.arassec.artivact.creator.ui.event.SceneEvent;
import com.arassec.artivact.creator.ui.event.SceneEventType;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@ConditionalOnProperty(value = "adapter.implementation.turntable", havingValue = "ArtivactTurntable")
public class ArtivactTurntableAdapter implements TurntableAdapter, ApplicationEventPublisherAware {

    @Value("${adapter.implementation.turntable.delay}")
    private int delay;

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void rotate(int numPhotos) {
        var turntableFound = new AtomicBoolean(false);
        var finished = new AtomicBoolean(false);

        SerialPort[] serialPorts = SerialPort.getCommPorts();
        final SerialPort liveSerialPort;

        for (SerialPort p : serialPorts) {
            Platform.runLater(() -> applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.STATUS_UPDATE, null, "status.turntable.checking-port")));

            log.debug("Checking serial port for artivact turntable {} - {} - {}", p.getSystemPortName(),
                    p.getDescriptivePortName(), p.getPortDescription());

            p.setComPortParameters(9600, 8, 1, 0); // default connection settings for Arduino
            p.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block until bytes can be written
            p.openPort();
            log.debug("Serial port open: {}", p.isOpen());

            if (p.isOpen()) {
                liveSerialPort = p;
                Platform.runLater(() -> applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.STATUS_UPDATE, null, "status.turntable.potential-port-found")));
                log.debug("Found potential artivact turntable at serial port: {}", liveSerialPort.getSystemPortName());

                var resultBuilder = new StringBuilder();

                liveSerialPort.addDataListener(new SerialPortDataListener() {
                    @Override
                    public void serialEvent(SerialPortEvent event) {
                        int size = event.getSerialPort().bytesAvailable();
                        var buffer = new byte[size];
                        event.getSerialPort().readBytes(buffer, size);
                        resultBuilder.append(new String(buffer));

                        String commandResult = resultBuilder.toString().trim();

                        if (commandResult.startsWith("artivact-tt-v1")) {
                            Platform.runLater(() -> applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.STATUS_UPDATE, null, "status.turntable.port-found")));
                            log.debug("Found artivact turntable at port: {}", liveSerialPort.getSystemPortName());
                            turntableFound.set(true);
                            resultBuilder.delete(0, resultBuilder.length());
                            try {
                                liveSerialPort.getOutputStream().write(("t" + numPhotos + "\n").getBytes());
                                liveSerialPort.getOutputStream().flush();
                            } catch (Exception e) {
                                throw new IllegalStateException("Could not send command to turntable!");
                            }
                        } else if (commandResult.startsWith("done")) {
                            Platform.runLater(() -> applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.STATUS_UPDATE, null, "status.turntable.rotation-finished")));
                            log.debug("Finished rotating artivact turntable.");
                            if (!liveSerialPort.closePort()) {
                                throw new IllegalStateException("Could not close serial port to turntable!");
                            }
                            finished.set(true);
                        }
                    }

                    @Override
                    public int getListeningEvents() {
                        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                    }
                });

                try {
                    Platform.runLater(() -> applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.STATUS_UPDATE,
                            null, "status.turntable.waiting-version-command")));
                    log.debug("Waiting before sending version command to artivact turntable.");
                    Thread.sleep(delay);
                    liveSerialPort.getOutputStream().write(("v\n").getBytes());
                    liveSerialPort.getOutputStream().flush();
                    Platform.runLater(() -> applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.STATUS_UPDATE,
                            null, "status.turntable.version-command")));
                    log.debug("Version command sent to artivact turntable.");
                } catch (IOException e) {
                    log.error("Exception during turntable operation!", e);
                } catch (InterruptedException e) {
                    log.error("Interrupted during turntable operation!", e);
                    Thread.currentThread().interrupt();
                }

                break;
            }
        }

        try {
            Thread.sleep(delay);

            Platform.runLater(() -> applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.STATUS_UPDATE,
                    null, "status.turntable.waiting-finish")));

            while (!finished.get() && turntableFound.get()) {
                Thread.sleep(delay);
                log.debug("Waiting for artivact turntable to finish...");
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for turntable to finish operation!", e);
            Thread.currentThread().interrupt();
        }
    }

}
