package com.arassec.artivact.adapter.out.database;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.ItemEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.ItemEntityRepository;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring-Configuration of the database adapter module.
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = ItemEntityRepository.class)
@EntityScan(basePackageClasses = ItemEntity.class)
@EnableTransactionManagement
public class DatabaseAdapterConfiguration {
}
