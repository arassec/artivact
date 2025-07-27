package com.arassec.artivact.adapter.out.database.jdbc.springdata.repository;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.ConfigurationEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Spring-Data repository for {@link ConfigurationEntity}s.
 */
public interface ConfigurationEntityRepository extends CrudRepository<ConfigurationEntity, String> {
}
