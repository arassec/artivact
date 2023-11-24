package com.arassec.artivact.backend.persistence;

import com.arassec.artivact.backend.persistence.model.ItemEntity;
import org.springframework.data.repository.CrudRepository;

public interface ItemEntityRepository extends CrudRepository<ItemEntity, String> {
}
