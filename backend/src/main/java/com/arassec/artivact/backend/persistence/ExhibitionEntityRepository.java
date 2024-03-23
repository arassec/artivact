package com.arassec.artivact.backend.persistence;

import com.arassec.artivact.backend.persistence.model.ExhibitionEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Spring-Data repository for {@link ExhibitionEntity}s.
 */
public interface ExhibitionEntityRepository extends CrudRepository<ExhibitionEntity, String> {
}
