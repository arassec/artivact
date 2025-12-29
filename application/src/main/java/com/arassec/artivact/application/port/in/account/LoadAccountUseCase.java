package com.arassec.artivact.application.port.in.account;

import com.arassec.artivact.domain.model.account.Account;

import java.util.List;
import java.util.Optional;

/**
 * Use case for load account operations.
 */
public interface LoadAccountUseCase {

    /**
     * Loads all accounts.
     *
     * @return List of all accounts.
     */
    List<Account> loadAll();

    /**
     * Loads all accounts except the one with the given username.
     *
     * @param username The username to exclude.
     *
     * @return List of accounts.
     */
    List<Account> loadAllExcept(String username);

    /**
     * Loads an account by username.
     *
     * @param username The username.
     *
     * @return The account, if found.
     */
    Optional<Account> loadByUsername(String username);

    /**
     * Loads an account by API token.
     *
     * @param apiToken The API token.
     *
     * @return The account, if found.
     */
    Optional<Account> loadByApiToken(String apiToken);

}
