package com.arassec.artivact.adapter.in.rest.controller;

import com.arassec.artivact.application.port.in.account.CreateAccountUseCase;
import com.arassec.artivact.application.port.in.account.DeleteAccountUseCase;
import com.arassec.artivact.application.port.in.account.LoadAccountUseCase;
import com.arassec.artivact.application.port.in.account.UpdateAccountUseCase;
import com.arassec.artivact.domain.model.account.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller for account administration.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account")
public class AccountController {

    /**
     * Use case for loading accounts.
     */
    private final LoadAccountUseCase loadAccountUseCase;

    /**
     * Use case to update accounts.
     */
    private final UpdateAccountUseCase updateAccountUseCase;

    /**
     * Use case to create new accounts.
     */
    private final CreateAccountUseCase createAccountUseCase;

    /**
     * Use case to delete accounts.
     */
    private final DeleteAccountUseCase deleteAccountUseCase;

    /**
     * Returns data about the current users account.
     *
     * @param authentication Spring-Security's {@link Authentication} of the current user.
     * @return Account information.
     */
    @GetMapping("/own")
    public Account getOwnAccount(Authentication authentication) {
        if (authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return loadAccountUseCase.loadByUsername(userDetails.getUsername()).orElse(null);
        }
        return null;
    }

    /**
     * Updates account settings of the current user.
     *
     * @param account        The account data to set.
     * @param authentication Spring-Security's {@link Authentication} of the current user.
     * @return Updated account information.
     */
    @PutMapping("/own")
    public Account updateOwnAccount(@RequestBody Account account, Authentication authentication) {
        if (account != null && authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return updateAccountUseCase.updateOwnAccount(userDetails.getUsername(), account);
        }
        return null;
    }

    /**
     * Returns data of all accounts (except the current user's one).
     *
     * @param authentication Spring-Security's {@link Authentication} of the current user.
     * @return All application accounts.
     */
    @GetMapping
    public List<Account> getAccounts(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return loadAccountUseCase.loadAllExcept(userDetails.getUsername());
    }

    /**
     * Creates a new application account.
     *
     * @param account The account to create.
     * @return The created account.
     */
    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        return createAccountUseCase.create(account);
    }

    /**
     * Updates an account other than the current user's one.
     *
     * @param account The account to update.
     * @return The updated account.
     */
    @PutMapping
    public Account updateAccount(@RequestBody Account account) {
        return updateAccountUseCase.update(account);
    }

    /**
     * Deletes the account with the given ID.
     *
     * @param id The account's ID.
     */
    @DeleteMapping(value = "/{id}")
    public void deleteAccount(@PathVariable int id) {
        deleteAccountUseCase.delete(id);
    }

}
