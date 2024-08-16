package com.arassec.artivact.persistence.jdbc;

import com.arassec.artivact.core.model.account.Account;
import com.arassec.artivact.persistence.jdbc.springdata.entity.AccountEntity;
import com.arassec.artivact.persistence.jdbc.springdata.repository.AccountEntityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcAccountRepository}.
 */
@ExtendWith(MockitoExtension.class)
public class JdbcAccountRepositoryTest {

    /**
     * Repository under test.
     */
    @InjectMocks
    private JdbcAccountRepository jdbcAccountRepository;

    /**
     * Spring-Data account repository mock.
     */
    @Mock
    private AccountEntityRepository accountEntityRepository;

    /**
     * Tests creating an account.
     */
    @Test
    void testCreate() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(23);
        accountEntity.setVersion(42);

        when(accountEntityRepository.save(any(AccountEntity.class))).thenReturn(accountEntity);

        Account account = Account.builder()
                .username("username")
                .password("password")
                .email("email")
                .apiToken("apiToken")
                .admin(true)
                .user(true)
                .build();

        Account createdAccount = jdbcAccountRepository.create(account);

        assertEquals(23, createdAccount.getId());
        assertEquals(42, createdAccount.getVersion());
        assertNull(createdAccount.getPassword());
    }

    /**
     * Tests saving an account.
     */
    @Test
    void testSave() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setVersion(42);

        when(accountEntityRepository.findById(23)).thenReturn(Optional.of(accountEntity));

        when(accountEntityRepository.save(accountEntity)).thenReturn(accountEntity);

        Account account = Account.builder()
                .id(23)
                .username("username")
                .password("password")
                .email("email")
                .apiToken("apiToken")
                .admin(true)
                .user(true)
                .build();

        Account savedAccount = jdbcAccountRepository.save(account);

        assertEquals(42, savedAccount.getVersion());
        assertNull(savedAccount.getPassword());

        assertEquals("username", accountEntity.getUsername());
        assertEquals("password", accountEntity.getPassword());
        assertEquals("email", accountEntity.getEmail());
        assertEquals("apiToken", accountEntity.getApiToken());
        assertEquals("USER,ADMIN", accountEntity.getRoles());
    }

    /**
     * Tests deleting an account.
     */
    @Test
    void testDelete() {
        Account account = Account.builder().id(23).build();
        jdbcAccountRepository.delete(account);
        verify(accountEntityRepository, times(1)).deleteById(23);
    }

    /**
     * Tests loading all accounts.
     */
    @Test
    void testFindAll() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(23);
        accountEntity.setRoles("USER,ADMIN");

        when(accountEntityRepository.findAll()).thenReturn(List.of(accountEntity));

        List<Account> accounts = jdbcAccountRepository.findAll();

        assertEquals(1, accounts.size());
        assertEquals(23, accounts.getFirst().getId());
        assertTrue(accounts.getFirst().getAdmin());
        assertTrue(accounts.getFirst().getUser());
    }

    /**
     * Tests loading an account by its ID.
     */
    @Test
    void testFindById() {
        assertTrue(jdbcAccountRepository.findById(23).isEmpty());

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(23);
        accountEntity.setRoles("");

        when(accountEntityRepository.findById(23)).thenReturn(Optional.of(accountEntity));

        assertTrue(jdbcAccountRepository.findById(23).isPresent());
    }

    /**
     * Tests finding an account by its username.
     */
    @Test
    void testFindByUsername() {
        assertTrue(jdbcAccountRepository.findByUsername("username").isEmpty());

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setUsername("username");
        accountEntity.setRoles("");

        when(accountEntityRepository.findByUsername("username")).thenReturn(Optional.of(accountEntity));

        assertTrue(jdbcAccountRepository.findByUsername("username").isPresent());
    }

    /**
     * Tests finding an account by its API token.
     */
    @Test
    void testFindByApiToken() {
        assertTrue(jdbcAccountRepository.findByApiToken("apiToken").isEmpty());

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setApiToken("apiToken");
        accountEntity.setRoles("");

        when(accountEntityRepository.findByApiToken("apiToken")).thenReturn(Optional.of(accountEntity));

        assertTrue(jdbcAccountRepository.findByApiToken("apiToken").isPresent());
    }

}
