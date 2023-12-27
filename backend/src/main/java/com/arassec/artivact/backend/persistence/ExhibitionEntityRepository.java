package com.arassec.artivact.backend.persistence;

import com.arassec.artivact.backend.persistence.model.ExhibitionEntity;
import org.springframework.data.repository.CrudRepository;

public interface ExhibitionEntityRepository extends CrudRepository<ExhibitionEntity, String> {
}
