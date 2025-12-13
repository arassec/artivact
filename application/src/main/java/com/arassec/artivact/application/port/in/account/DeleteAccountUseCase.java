package com.arassec.artivact.application.port.in.account;

/**
 * Use case for deleting accounts.
 */
public interface DeleteAccountUseCase {

    /**
     * Deletes the account with the specified ID.
     *
     * @param accountId The account ID.
     */
    void delete(int accountId);

}
