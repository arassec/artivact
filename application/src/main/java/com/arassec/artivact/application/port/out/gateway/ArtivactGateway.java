package com.arassec.artivact.application.port.out.gateway;

import java.nio.file.Path;

public interface ArtivactGateway {

    void importItem(String remoteServer, String apiToken, Path exportFile);

}
