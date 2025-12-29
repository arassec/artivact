package com.arassec.artivact.application.port.in.account;

import com.arassec.artivact.domain.model.account.Account;

/**
 * Use case for update account operations.
 */
public interface UpdateAccountUseCase {

    /**
     * Updates an existing account.
     *
     * @param account The account to update.
     *
     * @return The updated account.
     */
    Account update(Account account);

    /**
     * Updates the user's own account.
     *
     * @param originalUsername The original username.
     * @param account The account data with updates.
     *
     * @return The updated account.
     */
    Account updateOwnAccount(String originalUsername, Account account);

}
