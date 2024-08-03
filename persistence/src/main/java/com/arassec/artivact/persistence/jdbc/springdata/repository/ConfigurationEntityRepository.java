package com.arassec.artivact.persistence.jdbc.springdata.repository;

import com.arassec.artivact.persistence.jdbc.springdata.entity.ConfigurationEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Spring-Data repository for {@link ConfigurationEntity}s.
 */
public interface ConfigurationEntityRepository extends CrudRepository<ConfigurationEntity, String> {
}
