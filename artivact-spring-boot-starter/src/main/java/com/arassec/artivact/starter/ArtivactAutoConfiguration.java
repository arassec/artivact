package com.arassec.artivact.starter;

import com.arassec.artivact.backend.ArtivactBackendConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@Import({ArtivactBackendConfiguration.class})
@PropertySource("classpath:/artivact.properties")
public class ArtivactAutoConfiguration {
}
