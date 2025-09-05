package com.arassec.artivact.adapter.out.database.jdbc.springdata.repository;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.PageEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Spring-Data repository for {@link PageEntity}s.
 */
public interface PageEntityRepository extends CrudRepository<PageEntity, String> {

    /**
     * Finds the first page with the given alias.
     *
     * @return The index page.
     */
    Optional<PageEntity> findFirstByAlias(String alias);

}
