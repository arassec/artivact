package com.arassec.artivact.backend.service.model.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private Integer id;

    private Integer version;

    private String username;

    private String password;

    private String email;

    private String apiToken;

    private Boolean user;

    private Boolean admin;

}
