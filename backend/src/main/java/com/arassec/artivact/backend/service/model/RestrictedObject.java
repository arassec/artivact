package com.arassec.artivact.backend.service.model;

import java.util.Collection;
import java.util.Set;

/**
 * A restricted object.
 */
public interface RestrictedObject {

    /**
     * Returns the object's restrictions.
     *
     * @return The restrictions.
     */
    Set<String> getRestrictions();

    /**
     * Returns whether the
     *
     * @param roles The user's roles.
     * @return {@code true} if the object is forbidden for the current user, {@code false} otherwise.
     */
    default boolean isForbidden(Collection<String> roles) {
        if (getRestrictions().isEmpty()) {
            return false; // no restrictions
        }
        if (roles == null) {
            return true; // restrictions, but user has no roles
        }
        return getRestrictions().stream().noneMatch(roles::contains);
    }

}
