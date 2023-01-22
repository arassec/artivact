package com.arassec.artivact.vault.backend.service;

import com.arassec.artivact.vault.backend.persistence.model.AccountEntity;
import com.arassec.artivact.vault.backend.persistence.repository.AccountRepository;
import com.arassec.artivact.vault.backend.service.model.Account;
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

import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${artivact.vault.initial.username:}")
    private String initialUsername;

    @PostConstruct
    public void initialize() {
        if (StringUtils.hasText(initialUsername)) {
            if (accountRepository.findByUsername(initialUsername).isPresent()) {
                return;
            }

            String initialPassword = UUID.randomUUID().toString().split("-")[0];

            AccountEntity accountEntity = new AccountEntity();
            accountEntity.setUsername(initialUsername);
            accountEntity.setPassword(passwordEncoder.encode(initialPassword));
            accountEntity.setRoles("USER,ADMIN");
            accountEntity.setEnabled(true);

            accountRepository.save(accountEntity);

            log.info("##############################################################");
            log.info("Initial user created: {} / {}", initialUsername, initialPassword);
            log.info("##############################################################");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AccountEntity> accountEntityOptional = accountRepository.findByUsername(username);

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

    public Optional<Account> loadByUsername(String username) {
        Optional<AccountEntity> accountEntityOptional = accountRepository.findByUsername(username);
        if (accountEntityOptional.isPresent()) {
            AccountEntity accountEntity = accountEntityOptional.get();
            return Optional.of(Account.builder()
                    .id(accountEntity.getId())
                    .version(accountEntity.getVersion())
                    .username(accountEntity.getUsername())
                    .build());
        }
        return Optional.empty();
    }

    public void updateAccount(String originalUsername, Account account) {
        AccountEntity accountEntity = accountRepository.findById(account.getId()).orElseThrow();

        if (!accountEntity.getUsername().equals(originalUsername)) {
            throw new IllegalStateException("Account does not match logged in user!");
        }

        if (StringUtils.hasText(account.getUsername())) {
            accountEntity.setUsername(account.getUsername());
        }
        if (StringUtils.hasText(account.getPassword())) {
            accountEntity.setPassword(passwordEncoder.encode(account.getPassword()));
        }

        accountRepository.save(accountEntity);
    }
}
