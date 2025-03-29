package com.arassec.artivact.core.repository;

import com.arassec.artivact.core.model.page.Page;

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
     * Returns the first page which is configured to be the index page.
     *
     * @return The index page.
     */
    Page findIndexPage();

    /**
     * Returns the first page which has the given alias configured.
     *
     * @return The page with the given alias.
     */
    Optional<Page> findByAlias(String alias);

}
