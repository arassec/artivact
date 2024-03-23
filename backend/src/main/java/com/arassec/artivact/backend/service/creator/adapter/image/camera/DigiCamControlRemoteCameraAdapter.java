package com.arassec.artivact.backend.service.creator.adapter.image.camera;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.misc.ProgressMonitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Camera adapter for the Windows open source tool "DigiCamControl".
 * <p>
 * This implementation uses the <a href="https://digicamcontrol.com/doc/userguide/web">Web Interface</a>
 * of the application.
 */
@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class DigiCamControlRemoteCameraAdapter extends BaseCameraAdapter {

    /**
     * DigiCamControl API command to set the target folder for taken pictures.
     */
    private static final String SET_SESSION_FOLDER_CMD = "?slc=set&param1=session.folder&param2=";

    /**
     * DigiCamControl API command to capture a picture.
     */
    private static final String CAPTURE_CMD = "?slc=capture&param1=";

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ProgressMonitor progressMonitor, CameraInitParams initParams) {
        super.initialize(progressMonitor, initParams);
        executeRemoteCommand(initParams.getAdapterConfiguration(), SET_SESSION_FOLDER_CMD
                + URLEncoder.encode(initParams.getTargetDir().toAbsolutePath().toString(), StandardCharsets.UTF_8));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void captureImage(String filename) {
        executeRemoteCommand(initParams.getAdapterConfiguration(), CAPTURE_CMD + filename);
    }

    /**
     * Executes a DigiCamControl Web API command via HTTP.
     * <p>
     * Synchronized because the DigiCamControl HTTP server doesn't seem to be capable of parallel requests!
     *
     * @param adapterConfiguration The current adapter configuration.
     * @param command              The command to execute.
     */
    private synchronized void executeRemoteCommand(AdapterConfiguration adapterConfiguration, String command) {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(adapterConfiguration.getConfigValue(getSupportedImplementation()) + command))
                    .GET()
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException e) {
            throw new ArtivactException("Could not use DigiCamControl remotely: " + e.getClass().getSimpleName(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactException("Could not use DigiCamControl remotely: " + e.getClass().getSimpleName(), e);
        }
    }

}
