package com.arassec.artivact.application.port.out.adapter;

import java.nio.file.Path;

public interface ArtivactApiAdapter {

    void importItem(String remoteServer, String apiToken, Path exportFile);

}
