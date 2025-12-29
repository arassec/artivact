package com.arassec.artivact.application.port.in.account;

import com.arassec.artivact.domain.model.account.Account;

/**
 * Use case for create account operations.
 */
public interface CreateAccountUseCase {

    /**
     * Creates a new account.
     *
     * @param account The account to create.
     *
     * @return The created account.
     */
    Account create(Account account);

}
