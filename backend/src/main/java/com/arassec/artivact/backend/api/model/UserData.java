package com.arassec.artivact.backend.api.model;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
@Builder
public class UserData {

    private boolean authenticated;

    @Builder.Default
    private List<String> roles = new LinkedList<>();

    private String username;

}
