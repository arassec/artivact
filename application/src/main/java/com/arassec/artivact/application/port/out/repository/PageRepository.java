package com.arassec.artivact.application.port.out.repository;

import com.arassec.artivact.domain.model.page.Page;

import java.util.List;
import java.util.Optional;

/**
 * Repository for pages.
 */
public interface PageRepository {

    /**
     * Saves the given page.
     *
     * @param page The page to save.
     */
    void save(Page page);

    /**
     * Deletes the page with the given ID.
     *
     * @param pageId The page's ID.
     * @return The deleted page.
     */
    Optional<Page> deleteById(String pageId);

    /**
     * Returns all available pages.
     *
     * @return All available pages.
     */
    List<Page> findAll();

    /**
     * Loads the page with the given ID.
     *
     * @param pageId The page's ID.
     * @return The page with the given ID.
     */
    Optional<Page> findById(String pageId);

    /**
     * Returns the first page which has the given alias configured, or the page that has the parameter as ID.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @return The page with the given alias.
     */
    Optional<Page> findByIdOrAlias(String pageIdOrAlias);

}
