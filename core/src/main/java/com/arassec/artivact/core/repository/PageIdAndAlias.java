package com.arassec.artivact.core.repository;

/**
 * Contains a page's ID and alias if available.
 */
public interface PageIdAndAlias {

    /**
     * Returns the page's ID.
     *
     * @return The ID.
     */
    String getId();

    /**
     * Returns the page's alias, if available.
     *
     * @return The alias.
     */
    String getAlias();

}
