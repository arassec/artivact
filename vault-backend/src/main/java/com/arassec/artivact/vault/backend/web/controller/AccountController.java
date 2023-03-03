package com.arassec.artivact.vault.backend.web.controller;

import com.arassec.artivact.vault.backend.service.AccountService;
import com.arassec.artivact.vault.backend.service.model.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AccountController extends BaseController {

    private final AccountService accountService;

    @GetMapping("/account")
    public Account getOwnAccount(Authentication authentication) {
        if (authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return accountService.loadOwnAccount(userDetails.getUsername()).orElse(null);
        }
        return null;
    }

    @PutMapping("/account")
    public Account updateOwnAccount(@RequestBody Account account, Authentication authentication) {
        if (account != null && authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return accountService.updateOwnAccount(userDetails.getUsername(), account);
        }
        return null;
    }

    @GetMapping(value = "/administration/account")
    public List<Account> getAccounts(Authentication authentication) {
        if (isAdmin(authentication)) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return accountService.loadAllExcept(userDetails.getUsername());
        }
        return List.of();
    }

    @PutMapping("/administration/account")
    public Account updateAccount(@RequestBody Account account, Authentication authentication) {
        if (account != null && isAdmin(authentication)) {
            return accountService.updateAccount(account);
        }
        return null;
    }

    @PostMapping(value = "/administration/account")
    public Account createAccount(@RequestBody Account account, Authentication authentication) {
        if (account != null && isAdmin(authentication)) {
            return accountService.createAccount(account);
        }
        return null;
    }

    @DeleteMapping(value = "/administration/account/{id}")
    public void deleteAccount(@PathVariable int id, Authentication authentication) {
        if (isAdmin(authentication)) {
            accountService.deleteAccount(id);
        }
    }

}
