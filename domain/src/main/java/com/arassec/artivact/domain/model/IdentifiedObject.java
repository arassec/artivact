package com.arassec.artivact.domain.model;

/**
 * An object that can be identified by its ID.
 */
public interface IdentifiedObject {

    /**
     * Returns the object's ID.
     *
     * @return The ID.
     */
    String getId();

    /**
     * Sets the object's ID.
     *
     * @param id The ID to set.
     */
    void setId(String id);

}
