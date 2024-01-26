package com.arassec.artivact.backend.service.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeConfiguration {

    private String remoteServer;

    private String apiToken;

}
