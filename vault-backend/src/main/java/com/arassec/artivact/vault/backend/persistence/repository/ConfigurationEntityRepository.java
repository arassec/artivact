package com.arassec.artivact.vault.backend.persistence.repository;

import com.arassec.artivact.vault.backend.persistence.model.ConfigurationEntity;
import org.springframework.data.repository.CrudRepository;

public interface ConfigurationEntityRepository extends CrudRepository<ConfigurationEntity, String> {
}
