package com.arassec.artivact.persistence.jdbc.springdata.repository;

import com.arassec.artivact.core.repository.PageIdAndAlias;
import com.arassec.artivact.persistence.jdbc.springdata.entity.PageEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Spring-Data repository for {@link PageEntity}s.
 */
public interface PageEntityRepository extends CrudRepository<PageEntity, String> {

    /**
     * Finds the ID of the first index page.
     *
     * @param indexPage If set to {@code true}, the ID of the first index page is loaded.
     * @return The ID of the index page.
     */
    @Query(value = "SELECT id, alias FROM av_page WHERE index_page = :indexPage LIMIT 1", nativeQuery = true)
    Optional<PageIdAndAlias> findFirstIndexPageId(boolean indexPage);

    /**
     * Finds the first page with the given alias.
     *
     * @return The index page.
     */
    Optional<PageEntity> findFirstByAlias(String alias);

}
