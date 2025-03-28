package com.arassec.artivact.persistence.jdbc.springdata.repository;

import com.arassec.artivact.persistence.jdbc.springdata.entity.PageEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Spring-Data repository for {@link PageEntity}s.
 */
public interface PageEntityRepository extends CrudRepository<PageEntity, String> {

    /**
     * Finds the first index page.
     *
     * @param indexPage If set to {@code true}, the first index page is loaded.
     * @return The index page.
     */
    Optional<PageEntity> findFirstByIndexPage(boolean indexPage);

    /**
     * Finds the first page with the given alias.
     *
     * @return The index page.
     */
    Optional<PageEntity> findFirstByAlias(String alias);

}
