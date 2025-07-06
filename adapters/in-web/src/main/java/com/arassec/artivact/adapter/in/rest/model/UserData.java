package com.arassec.artivact.adapter.in.rest.model;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains data about the current user.
 */
@Data
@Builder
public class UserData {

    /**
     * {@code true} if the user has been authenticatd, {@code false} otherwise.
     */
    private boolean authenticated;

    /**
     * The user's roles.
     */
    @Builder.Default
    private List<String> roles = new LinkedList<>();

    /**
     * The username.
     */
    private String username;

}
