package com.arassec.artivact.application.port.out.gateway;

import java.nio.file.Path;

/**
 * Interface for artivact gateway.
 */
public interface ArtivactGateway {

    /**
     * Imports an item from a remote artivact server.
     *
     * @param remoteServer The remote server's URL.
     * @param apiToken     The API token to use for authentication.
     * @param exportFile   Path to the exported item file.
     */
    void importItem(String remoteServer, String apiToken, Path exportFile);

}
