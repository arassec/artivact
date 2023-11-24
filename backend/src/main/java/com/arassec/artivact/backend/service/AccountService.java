package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.AccountEntityRepository;
import com.arassec.artivact.backend.persistence.model.AccountEntity;
import com.arassec.artivact.backend.service.model.Roles;
import com.arassec.artivact.backend.service.model.account.Account;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private static final String INITIAL_USERNAME = "admin";

    private final AccountEntityRepository accountEntityRepository;

    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initialize() {
        if (!accountEntityRepository.findAll().iterator().hasNext()) {

            String initialPassword = UUID.randomUUID().toString().split("-")[0];

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

    public Account updateOwnAccount(String originalUsername, Account account) {
        AccountEntity accountEntity = accountEntityRepository.findById(account.getId()).orElseThrow();

        if (originalUsername == null || !originalUsername.equals(accountEntity.getUsername())) {
            throw new IllegalStateException("Account does not match logged in user!");
        }

        if (StringUtils.hasText(account.getUsername())) {
            accountEntity.setUsername(account.getUsername());
        }

        if (StringUtils.hasText(account.getPassword())) {
            accountEntity.setPassword(passwordEncoder.encode(account.getPassword()));
        }

        accountEntity.setEmail(account.getEmail());

        AccountEntity savedAccountEntity = accountEntityRepository.save(accountEntity);

        account.setVersion(savedAccountEntity.getVersion());

        return account;
    }

    public List<Account> loadAllExcept(String username) {
        return StreamSupport.stream(accountEntityRepository.findAll().spliterator(), false)
                .filter(entity -> !entity.getUsername().equals(username))
                .map(this::mapEntity)
                .toList();
    }

    public Account update(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account must be set to update it!");
        }

        AccountEntity accountEntity = accountEntityRepository.findById(account.getId()).orElseThrow();

        if (StringUtils.hasText(account.getUsername())) {
            accountEntity.setUsername(account.getUsername());
        }

        if (StringUtils.hasText(account.getPassword())) {
            accountEntity.setPassword(passwordEncoder.encode(account.getPassword()));
        }

        accountEntity.setEmail(account.getEmail());
        accountEntity.setRoles(getRoles(account));

        AccountEntity savedAccountEntity = accountEntityRepository.save(accountEntity);

        account.setVersion(savedAccountEntity.getVersion());
        account.setPassword(null);

        return account;
    }

    public Account create(Account account) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setUsername(account.getUsername());
        accountEntity.setPassword(passwordEncoder.encode(account.getPassword()));
        accountEntity.setEmail(account.getEmail());
        accountEntity.setRoles(getRoles(account));

        AccountEntity savedAccountEntity = accountEntityRepository.save(accountEntity);

        account.setId(savedAccountEntity.getId());
        account.setVersion(savedAccountEntity.getVersion());
        account.setPassword(null);

        return account;
    }

    public void delete(int accountId) {
        accountEntityRepository.deleteById(accountId);
    }

    private Account mapEntity(AccountEntity accountEntity) {
        return Account.builder()
                .id(accountEntity.getId())
                .version(accountEntity.getVersion())
                .username(accountEntity.getUsername())
                .email(accountEntity.getEmail())
                .user(hasRole(accountEntity.getRoles(), Roles.USER))
                .admin(hasRole(accountEntity.getRoles(), Roles.ADMIN))
                .build();
    }

    private boolean hasRole(String roles, String role) {
        return List.of(roles.split(",")).contains(role);
    }

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
