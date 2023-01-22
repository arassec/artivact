package com.arassec.artivact.vault.starter;

import com.arassec.artivact.vault.backend.ArtivactVaultBackendConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@Import({ArtivactVaultBackendConfiguration.class})
@PropertySource("classpath:/artivact-vault.properties")
public class ArtivactVaultAutoConfiguration {
}
