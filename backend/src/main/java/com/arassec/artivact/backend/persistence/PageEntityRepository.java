package com.arassec.artivact.backend.persistence;

import com.arassec.artivact.backend.persistence.model.PageEntity;
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

}
