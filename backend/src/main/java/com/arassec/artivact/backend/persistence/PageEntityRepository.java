package com.arassec.artivact.backend.persistence;

import com.arassec.artivact.backend.persistence.model.PageEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PageEntityRepository extends CrudRepository<PageEntity, String> {

    Optional<PageEntity> findFirstByIndexPage(boolean indexPage);

}
