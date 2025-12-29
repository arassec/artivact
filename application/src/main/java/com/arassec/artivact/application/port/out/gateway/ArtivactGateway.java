package com.arassec.artivact.application.port.out.gateway;

import java.nio.file.Path;

/**
 * Interface for artivact gateway.
 */
public interface ArtivactGateway {

    void importItem(String remoteServer, String apiToken, Path exportFile);

}
