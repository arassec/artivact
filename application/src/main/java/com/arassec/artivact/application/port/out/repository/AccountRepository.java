package com.arassec.artivact.application.port.out.repository;

import com.arassec.artivact.domain.model.account.Account;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Accounts.
 */
public interface AccountRepository {

    /**
     * Creates a new account.
     *
     * @param account The initial account data to use.
     * @return The newly created account.
     */
    Account create(Account account);

    /**
     * Saves the given account.
     *
     * @param account The account to save.
     * @return The updated account.
     */
    Account save(Account account);

    /**
     * Deletes an account.
     *
     * @param account The account to delete.
     */
    void delete(Account account);

    /**
     * Returns all available accounts.
     *
     * @return All available accounts.
     */
    List<Account> findAll();

    /**
     * Returns the account with the given ID.
     *
     * @param id The account's ID.
     * @return The account with the given ID.
     */
    Optional<Account> findById(Integer id);

    /**
     * Returns the account with the given username:
     *
     * @param username The account's username.
     * @return The account with the given username.
     */
    Optional<Account> findByUsername(String username);

    /**
     * Returns the account with the given API-Token.
     *
     * @param apiToken The accounts API-Token.
     * @return The account with the given token.
     */
    Optional<Account> findByApiToken(String apiToken);

}
