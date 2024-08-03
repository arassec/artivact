package com.arassec.artivact.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configures the persistence layer for igor.
 */
@Configuration
@ComponentScan
@EnableJpaRepositories
@EntityScan
@EnableTransactionManagement
public class PersistenceConfiguration {

}
