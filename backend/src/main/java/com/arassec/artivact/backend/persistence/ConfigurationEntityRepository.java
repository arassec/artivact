package com.arassec.artivact.backend.persistence;

import com.arassec.artivact.backend.persistence.model.ConfigurationEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Spring-Data repository for {@link ConfigurationEntity}s.
 */
public interface ConfigurationEntityRepository extends CrudRepository<ConfigurationEntity, String> {
}
