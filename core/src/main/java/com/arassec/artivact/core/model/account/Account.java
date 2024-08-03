package com.arassec.artivact.core.model.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An Artivact account.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /**
     * The account's ID.
     */
    private Integer id;

    /**
     * Technical version.
     */
    private Integer version;

    /**
     * The account's username.
     */
    private String username;

    /**
     * The account's password.
     */
    private String password;

    /**
     * The account's e-mail address.
     */
    private String email;

    /**
     * An API token for remote syncing of items using this account.
     */
    private String apiToken;

    /**
     * If {@code true}, the account has the user role assigned.
     */
    private Boolean user;

    /**
     * If {@code true}, the account has the administrator role assigned.
     */
    private Boolean admin;

}
