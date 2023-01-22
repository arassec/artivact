package com.arassec.artivact.vault.backend.persistence.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "av_account")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    private String username;

    private String password;

    private boolean enabled;

    private String email;

    private String firstname;

    private String lastname;

    private String roles;

}
