package com.arassec.artivact.domain.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for item syncing.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeConfiguration {

    /**
     * HTTP-URL to the remote server to sync items with.
     */
    private String remoteServer;

    /**
     * The API token to use for access to the remote server.
     */
    private String apiToken;

}
