package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.AccountService;
import com.arassec.artivact.backend.service.model.account.Account;
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
@RequestMapping("/api")
public class AccountController {

    /**
     * The application's {@link AccountService}.
     */
    private final AccountService accountService;

    /**
     * Returns data about the current users account.
     *
     * @param authentication Spring-Security's {@link Authentication} of the current user.
     * @return Account information.
     */
    @GetMapping("/account/own")
    public Account getOwnAccount(Authentication authentication) {
        if (authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return accountService.loadOwnAccount(userDetails.getUsername()).orElse(null);
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
    @PutMapping("/account/own")
    public Account updateOwnAccount(@RequestBody Account account, Authentication authentication) {
        if (account != null && authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return accountService.updateOwnAccount(userDetails.getUsername(), account);
        }
        return null;
    }

    /**
     * Returns data of all accounts (except the current user's one).
     *
     * @param authentication Spring-Security's {@link Authentication} of the current user.
     * @return All application accounts.
     */
    @GetMapping(value = "/account")
    public List<Account> getAccounts(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return accountService.loadAllExcept(userDetails.getUsername());
    }

    /**
     * Creates a new application account.
     *
     * @param account The account to create.
     * @return The created account.
     */
    @PostMapping(value = "/account")
    public Account createAccount(@RequestBody Account account) {
        return accountService.create(account);
    }

    /**
     * Updates an account other than the current user's one.
     *
     * @param account The account to update.
     * @return The updated account.
     */
    @PutMapping("/account")
    public Account updateAccount(@RequestBody Account account) {
        return accountService.update(account);
    }

    /**
     * Deletes the account with the given ID.
     *
     * @param id The account's ID.
     */
    @DeleteMapping(value = "/account/{id}")
    public void deleteAccount(@PathVariable int id) {
        accountService.delete(id);
    }

}
