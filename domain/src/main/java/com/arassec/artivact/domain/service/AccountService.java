package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.model.account.Account;
import com.arassec.artivact.core.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing application accounts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    /**
     * Repository for accounts.
     */
    private final AccountRepository accountRepository;

    /**
     * The system's password encoder.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     */
    public Optional<Account> loadByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    /**
     * Updates the current user's account data.
     *
     * @param originalUsername The original username.
     * @param account          The account data to update.
     * @return The updated account.
     */
    public Account updateOwnAccount(String originalUsername, Account account) {

        Account persistedAccount = accountRepository.findById(account.getId()).orElseThrow();

        if (originalUsername == null || !originalUsername.equals(persistedAccount.getUsername())) {
            throw new IllegalStateException("Account does not match logged in user!");
        }

        updateValues(account, persistedAccount);

        persistedAccount = accountRepository.save(persistedAccount);

        account.setVersion(persistedAccount.getVersion());

        return account;
    }

    /**
     * Loads an account by its API token.
     *
     * @param apiToken The API token of the account to load.
     * @return The account.
     */
    public Optional<Account> loadByApiToken(String apiToken) {
        if (!StringUtils.hasText(apiToken)) {
            return Optional.empty();
        }
        return accountRepository.findByApiToken(apiToken);
    }

    /**
     * Loads all accounts except the one with the given username.
     *
     * @param username The username of the account to exclude from the result.
     * @return Accounts.
     */
    public List<Account> loadAllExcept(String username) {
        return accountRepository.findAll().stream()
                .filter(account -> !account.getUsername().equals(username))
                .toList();
    }

    /**
     * Creates a new account.
     *
     * @param account The initial account data to use.
     * @return The newly created account.
     */
    public Account create(Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.create(account);
    }

    /**
     * Updates the given account data.
     *
     * @param account The account to update.
     * @return The updated account.
     */
    public Account update(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account must be set to update it!");
        }

        Account persistedAccount = accountRepository.findById(account.getId()).orElseThrow();

        updateValues(account, persistedAccount);

        persistedAccount.setUser(account.getUser());
        persistedAccount.setAdmin(account.getAdmin());

        persistedAccount = accountRepository.save(persistedAccount);

        account.setVersion(persistedAccount.getVersion());

        return account;
    }

    /**
     * Deletes the account with the given ID.
     *
     * @param accountId The account's ID.
     */
    public void delete(int accountId) {
        accountRepository.delete(accountRepository.findById(accountId).orElseThrow());
    }

    /**
     * Updates the destination account with the values from the source account.
     *
     * @param srcAccount  The source account.
     * @param destAccount The account to update.
     */
    private void updateValues(Account srcAccount, Account destAccount) {
        if (StringUtils.hasText(srcAccount.getUsername())) {
            destAccount.setUsername(srcAccount.getUsername());
        }

        if (StringUtils.hasText(srcAccount.getPassword())) {
            destAccount.setPassword(passwordEncoder.encode(srcAccount.getPassword()));
        }

        destAccount.setEmail(srcAccount.getEmail());
        destAccount.setApiToken(srcAccount.getApiToken());
    }

}
