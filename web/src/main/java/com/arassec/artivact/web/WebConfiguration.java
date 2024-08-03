package com.arassec.artivact.web;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Configures the web layer of artivact.
 */
@Configuration
@ComponentScan
@EnableWebSecurity
public class WebConfiguration {
}
