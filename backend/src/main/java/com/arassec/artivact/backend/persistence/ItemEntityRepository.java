package com.arassec.artivact.backend.persistence;

import com.arassec.artivact.backend.persistence.model.ItemEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ItemEntityRepository extends PagingAndSortingRepository<ItemEntity, String>, CrudRepository<ItemEntity, String> {
}
