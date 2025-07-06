package com.arassec.artivact.starter;

import com.arassec.artivact.adapter.out.database.springdata.entity.ItemEntity;
import com.arassec.artivact.adapter.out.database.springdata.repository.ItemEntityRepository;
import com.arassec.artivact.application.ApplicationConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring-Boot autoconfiguration for the Artivact application.
 */
@Configuration
@ComponentScan(basePackages = "com.arassec.artivact")
@Import({ApplicationConfiguration.class})
@PropertySource("classpath:/artivact.properties")
@EnableWebSecurity
@EnableJpaRepositories(basePackageClasses = ItemEntityRepository.class)
@EntityScan(basePackageClasses = ItemEntity.class)
@EnableTransactionManagement
public class ArtivactAutoConfiguration {
}
