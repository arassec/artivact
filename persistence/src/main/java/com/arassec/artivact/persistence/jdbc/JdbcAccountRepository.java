package com.arassec.artivact.persistence.jdbc;

import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.account.Account;
import com.arassec.artivact.core.repository.AccountRepository;
import com.arassec.artivact.persistence.jdbc.springdata.entity.AccountEntity;
import com.arassec.artivact.persistence.jdbc.springdata.repository.AccountEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * {@link AccountRepository} implementation that uses JDBC for data access.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcAccountRepository implements AccountRepository {

    /**
     * Spring-Data's repository for {@link AccountEntity}s.
     */
    private final AccountEntityRepository accountEntityRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Account create(Account account) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setUsername(account.getUsername());
        accountEntity.setPassword(account.getPassword());
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
     * {@inheritDoc}
     */
    @Override
    public Account save(Account account) {
        AccountEntity accountEntity = accountEntityRepository.findById(account.getId()).orElseThrow();
        accountEntity.setUsername(account.getUsername());
        if (account.getPassword() != null) {
            accountEntity.setPassword(account.getPassword());
        }
        accountEntity.setEmail(account.getEmail());
        accountEntity.setApiToken(account.getApiToken());
        accountEntity.setRoles(getRoles(account));

        AccountEntity savedAccountEntity = accountEntityRepository.save(accountEntity);

        account.setVersion(savedAccountEntity.getVersion());
        account.setPassword(null);

        return account;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Account account) {
        accountEntityRepository.deleteById(account.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Account> findAll() {
        return StreamSupport.stream(accountEntityRepository.findAll().spliterator(), false)
                .map(this::mapEntity)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Account> findById(Integer id) {
        Optional<AccountEntity> accountEntityOptional = accountEntityRepository.findById(id);

        if (accountEntityOptional.isPresent()) {
            AccountEntity accountEntity = accountEntityOptional.get();
            return Optional.of(mapEntity(accountEntity));
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Account> findByUsername(String username) {
        Optional<AccountEntity> accountEntityOptional = accountEntityRepository.findByUsername(username);

        if (accountEntityOptional.isPresent()) {
            AccountEntity accountEntity = accountEntityOptional.get();
            return Optional.of(mapEntity(accountEntity));
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Account> findByApiToken(String apiToken) {
        Optional<AccountEntity> accountEntityOptional = accountEntityRepository.findByApiToken(apiToken);
        return accountEntityOptional.map(this::mapEntity);
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
                .password(accountEntity.getPassword())
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
