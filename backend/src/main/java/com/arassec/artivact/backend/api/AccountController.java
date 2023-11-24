package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.AccountService;
import com.arassec.artivact.backend.service.model.account.Account;
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
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/account/own")
    public Account getOwnAccount(Authentication authentication) {
        if (authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return accountService.loadOwnAccount(userDetails.getUsername()).orElse(null);
        }
        return null;
    }

    @PutMapping("/account/own")
    public Account updateOwnAccount(@RequestBody Account account, Authentication authentication) {
        if (account != null && authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return accountService.updateOwnAccount(userDetails.getUsername(), account);
        }
        return null;
    }

    @GetMapping(value = "/account")
    public List<Account> getAccounts(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return accountService.loadAllExcept(userDetails.getUsername());
    }

    @PutMapping("/account")
    public Account updateAccount(@RequestBody Account account) {
        return accountService.update(account);
    }

    @PostMapping(value = "/account")
    public Account createAccount(@RequestBody Account account) {
        return accountService.create(account);
    }

    @DeleteMapping(value = "/account/{id}")
    public void deleteAccount(@PathVariable int id) {
        accountService.delete(id);
    }

}
