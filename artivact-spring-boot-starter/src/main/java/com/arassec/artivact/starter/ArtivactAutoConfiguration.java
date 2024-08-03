package com.arassec.artivact.starter;

import com.arassec.artivact.domain.DomainConfiguration;
import com.arassec.artivact.persistence.PersistenceConfiguration;
import com.arassec.artivact.web.WebConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring-Boot autoconfiguration for the Artivact application.
 */
@Configuration
@ComponentScan
@Import({DomainConfiguration.class, PersistenceConfiguration.class, WebConfiguration.class})
@PropertySource("classpath:/artivact.properties")
public class ArtivactAutoConfiguration {
}
