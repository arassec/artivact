package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.AccountEntityRepository;
import com.arassec.artivact.backend.persistence.model.AccountEntity;
import com.arassec.artivact.backend.service.model.Roles;
import com.arassec.artivact.backend.service.model.account.Account;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

/**
 * Service for managing application accounts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    /**
     * The initial user's username.
     */
    private static final String INITIAL_USERNAME = "admin";

    /**
     * Repository for accounts.
     */
    private final AccountEntityRepository accountEntityRepository;

    /**
     * Password encoder.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Initial administrator password. Can be set per JVM parameter for integration testing.
     */
    @Value("${artivact.initial.password:}")
    private String initialPassword;

    /**
     * Initializes the application by creating an initial user account if none is available. The password is printed
     * to the application's log.
     */
    @PostConstruct
    public void initialize() {
        if (!accountEntityRepository.findAll().iterator().hasNext()) {

            if (!StringUtils.hasText(initialPassword)) {
                initialPassword = UUID.randomUUID().toString().split("-")[0];
            }

            create(Account.builder()
                    .username(INITIAL_USERNAME)
                    .password(initialPassword)
                    .user(true)
                    .admin(true)
                    .build());

            log.info("");
            log.info("##############################################################");
            log.info("Initial user created: {} / {}", INITIAL_USERNAME, initialPassword);
            log.info("##############################################################");
            log.info("");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AccountEntity> accountEntityOptional = accountEntityRepository.findByUsername(username);

        if (accountEntityOptional.isPresent()) {
            AccountEntity accountEntity = accountEntityOptional.get();

            return User.builder()
                    .username(accountEntity.getUsername())
                    .password(accountEntity.getPassword())
                    .roles(accountEntity.getRoles().split(","))
                    .build();
        }

        throw new UsernameNotFoundException("No account with username " + username + " found!");
    }

    /**
     * Loads the current user's account data.
     *
     * @param username The username.
     * @return The account.
     */
    @SuppressWarnings("java:S3516") // Sonar false positive
    public Optional<Account> loadOwnAccount(String username) {
        Optional<AccountEntity> accountEntityOptional = accountEntityRepository.findByUsername(username);
        if (accountEntityOptional.isPresent()) {
            AccountEntity accountEntity = accountEntityOptional.get();
            Account account = mapEntity(accountEntity);
            account.setUser(null);
            account.setPassword(null);
            return Optional.of(account);
        }
        return Optional.empty();
    }

    /**
     * Updates the current user's account data.
     *
     * @param originalUsername The original username.
     * @param account          The account data to update.
     * @return The updated account.
     */
    public Account updateOwnAccount(String originalUsername, Account account) {
        AccountEntity accountEntity = accountEntityRepository.findById(account.getId()).orElseThrow();

        if (originalUsername == null || !originalUsername.equals(accountEntity.getUsername())) {
            throw new IllegalStateException("Account does not match logged in user!");
        }

        updateEntityValues(account, accountEntity);

        AccountEntity savedAccountEntity = accountEntityRepository.save(accountEntity);

        account.setVersion(savedAccountEntity.getVersion());

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
        Optional<AccountEntity> accountEntityOptional = accountEntityRepository.findByApiToken(apiToken);
        return accountEntityOptional.map(this::mapEntity);
    }

    /**
     * Loads all accounts except the one with the given username.
     *
     * @param username The username of the account to exclude from the result.
     * @return Accounts.
     */
    public List<Account> loadAllExcept(String username) {
        return StreamSupport.stream(accountEntityRepository.findAll().spliterator(), false)
                .filter(entity -> !entity.getUsername().equals(username))
                .map(this::mapEntity)
                .toList();
    }

    /**
     * Creates a new account.
     *
     * @param account The initial account data to use.
     * @return The newly created account.
     */
    public Account create(Account account) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setUsername(account.getUsername());
        accountEntity.setPassword(passwordEncoder.encode(account.getPassword()));
        accountEntity.setEmail(account.getEmail());
        accountEntity.setApiToken(account.getApiToken());
        accountEntity.setRoles(getRoles(account));

        AccountEntity savedAccountEntity = accountEntityRepository.save(accountEntity);

        account.setId(savedAccountEntity.getId());
        account.setVersion(savedAccountEntity.getVersion());
        account.setPassword(null);

        return account;
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

        AccountEntity accountEntity = accountEntityRepository.findById(account.getId()).orElseThrow();

        updateEntityValues(account, accountEntity);

        accountEntity.setRoles(getRoles(account));

        AccountEntity savedAccountEntity = accountEntityRepository.save(accountEntity);

        account.setVersion(savedAccountEntity.getVersion());
        account.setPassword(null);

        return account;
    }

    /**
     * Updates the entity with the values from the account.
     *
     * @param account       The account.
     * @param accountEntity The account entity to update.
     */
    private void updateEntityValues(Account account, AccountEntity accountEntity) {
        if (StringUtils.hasText(account.getUsername())) {
            accountEntity.setUsername(account.getUsername());
        }

        if (StringUtils.hasText(account.getPassword())) {
            accountEntity.setPassword(passwordEncoder.encode(account.getPassword()));
        }

        accountEntity.setEmail(account.getEmail());
        accountEntity.setApiToken(account.getApiToken());
    }

    /**
     * Deletes the account with the given ID.
     *
     * @param accountId The account's ID.
     */
    public void delete(int accountId) {
        accountEntityRepository.deleteById(accountId);
    }

    /**
     * Maps the persistence {@link AccountEntity} to the {@link Account} object.
     *
     * @param accountEntity The persistence entity.
     * @return The account.
     */
    private Account mapEntity(AccountEntity accountEntity) {
        return Account.builder()
                .id(accountEntity.getId())
                .version(accountEntity.getVersion())
                .username(accountEntity.getUsername())
                .email(accountEntity.getEmail())
                .apiToken(accountEntity.getApiToken())
                .user(hasRole(accountEntity.getRoles(), Roles.USER))
                .admin(hasRole(accountEntity.getRoles(), Roles.ADMIN))
                .build();
    }

    /**
     * Returns whether the comma-separated list of roles contains the given role.
     *
     * @param roles Comma-separated list of roles.
     * @param role  The role to check.
     * @return {@code true}, if the roles contain the given role. {@code false} otherwise.
     */
    private boolean hasRole(String roles, String role) {
        return List.of(roles.split(",")).contains(role);
    }

    /**
     * Returns the account's roles.
     *
     * @param account The account to get the roles for.
     * @return The roles as comma-separated list.
     */
    private String getRoles(Account account) {
        List<String> roles = new LinkedList<>();
        if (account.getUser() != null && account.getUser()) {
            roles.add(Roles.USER);
        }
        if (account.getAdmin() != null && account.getAdmin()) {
            roles.add(Roles.ADMIN);
        }
        return String.join(",", roles);
    }

}
