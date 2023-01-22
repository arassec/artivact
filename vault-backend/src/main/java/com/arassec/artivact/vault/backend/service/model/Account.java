package com.arassec.artivact.vault.backend.service.model;

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

}
