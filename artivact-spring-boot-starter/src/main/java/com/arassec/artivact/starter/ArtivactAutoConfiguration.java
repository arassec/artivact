package com.arassec.artivact.starter;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring-Boot autoconfiguration for the Artivact application.
 */
@Configuration
@ComponentScan(basePackages = "com.arassec.artivact")
@PropertySource("classpath:/artivact.properties")
public class ArtivactAutoConfiguration {
}
