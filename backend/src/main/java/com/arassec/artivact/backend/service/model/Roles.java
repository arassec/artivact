package com.arassec.artivact.backend.service.model;

import java.util.List;

public class Roles {

    private Roles() {
    }

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String ROLE_USER = "ROLE_USER";

    public static final String ADMIN = "ADMIN";

    public static final String USER = "USER";

    public static List<String> getAvailableRoles() {
        return List.of(ROLE_ADMIN, ROLE_USER);
    }

}
