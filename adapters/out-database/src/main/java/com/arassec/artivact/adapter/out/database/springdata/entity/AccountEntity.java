package com.arassec.artivact.adapter.out.database.springdata.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Account data.
 */
@Data
@Entity
@Table(name = "av_account")
public class AccountEntity {

    /**
     * Account ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Technical version.
     */
    @Version
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
     * An e-mail address.
     */
    private String email;

    /**
     * An API token for remote syncing of items with this user account.
     */
    @Column(name = "api_token")
    private String apiToken;

    /**
     * This account's assigned roles.
     */
    private String roles;

}
