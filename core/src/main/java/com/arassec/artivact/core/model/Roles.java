package com.arassec.artivact.core.model;

/**
 * Defines the roles available for users in the application.
 */
public class Roles {

    /**
     * Creates a new instance.
     */
    private Roles() {
        // prevent instantiation.
    }

    /**
     * Administrator role with ROLE_-prefix.
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * User role with ROLE_-prefix.
     */
    public static final String ROLE_USER = "ROLE_USER";

    /**
     * Administrator role.
     */
    public static final String ADMIN = "ADMIN";

    /**
     * User role.
     */
    public static final String USER = "USER";

}
