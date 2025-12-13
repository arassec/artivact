package com.arassec.artivact.application.service.account;

import com.arassec.artivact.application.port.out.repository.AccountRepository;
import com.arassec.artivact.application.port.out.repository.FavoriteRepository;
import com.arassec.artivact.domain.model.account.Account;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService service;

    @Test
    void testLoadAllAccounts() {
        Account a1 = new Account();
        Account a2 = new Account();
        when(accountRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Account> result = service.loadAll();

        assertThat(result).containsExactly(a1, a2);
    }

    @Test
    void testLoadByUsername() {
        Account account = new Account();
        when(accountRepository.findByUsername("user")).thenReturn(Optional.of(account));

        Optional<Account> result = service.loadByUsername("user");

        assertThat(result).contains(account);
    }

    @Test
    void testLoadsByApiTokenWithToken() {
        Account account = new Account();
        when(accountRepository.findByApiToken("token")).thenReturn(Optional.of(account));

        Optional<Account> result = service.loadByApiToken("token");

        assertThat(result).contains(account);
    }

    @Test
    void testLoadsByApiTokenWithoutToken() {
        Optional<Account> result = service.loadByApiToken("   ");
        assertThat(result).isEmpty();
        verifyNoInteractions(accountRepository);
    }

    @Test
    void testLoadAllExceptUsername() {
        Account a1 = new Account();
        a1.setUsername("keep");
        Account a2 = new Account();
        a2.setUsername("skip");

        when(accountRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Account> result = service.loadAllExcept("skip");

        assertThat(result).containsExactly(a1);
    }

    @Test
    void testUpdateAccountValues() {
        Account incoming = new Account();
        incoming.setId(1);
        incoming.setUsername("newUser");
        incoming.setPassword("plainPw");
        incoming.setEmail("mail");
        incoming.setApiToken("api");
        incoming.setUser(true);
        incoming.setAdmin(true);

        Account persisted = new Account();
        persisted.setId(1);
        persisted.setVersion(5);

        when(accountRepository.findById(1)).thenReturn(Optional.of(persisted));
        when(passwordEncoder.encode("plainPw")).thenReturn("encoded");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account arg = inv.getArgument(0);
            arg.setVersion(6);
            return arg;
        });

        Account result = service.update(incoming);

        assertThat(result.getVersion()).isEqualTo(6);
        assertThat(persisted.getUsername()).isEqualTo("newUser");
        assertThat(persisted.getPassword()).isEqualTo("encoded");
        assertThat(persisted.getEmail()).isEqualTo("mail");
        assertThat(persisted.getApiToken()).isEqualTo("api");
        assertThat(persisted.getUser()).isTrue();
        assertThat(persisted.getAdmin()).isTrue();
    }

    @Test
    void testUpdateThrowsOnNull() {
        assertThatThrownBy(() -> service.update(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account must be set");
    }

    @Test
    void testUpdatesOwnAccountSuccessfully() {
        Account incoming = new Account();
        incoming.setId(1);
        incoming.setPassword("pw");

        Account persisted = new Account();
        persisted.setId(1);
        persisted.setUsername("me");

        when(accountRepository.findById(1)).thenReturn(Optional.of(persisted));
        when(passwordEncoder.encode("pw")).thenReturn("enc");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account arg = inv.getArgument(0);
            arg.setVersion(42);
            return arg;
        });

        Account result = service.updateOwnAccount("me", incoming);

        assertThat(result.getVersion()).isEqualTo(42);
        assertThat(persisted.getPassword()).isEqualTo("enc");
    }

    @Test
    void testUpdateOwnAccountThrowsOnWrongUser() {
        Account incoming = new Account();
        incoming.setId(1);
        Account persisted = new Account();
        persisted.setId(1);
        persisted.setUsername("other");

        when(accountRepository.findById(1)).thenReturn(Optional.of(persisted));

        assertThatThrownBy(() -> service.updateOwnAccount("me", incoming))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void testCreatesNewAccount() {
        Account newAccount = new Account();
        newAccount.setPassword("plain");

        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(accountRepository.create(any(Account.class))).thenReturn(newAccount);

        Account result = service.create(newAccount);

        assertThat(result.getPassword()).isEqualTo("encoded");
        verify(accountRepository).create(newAccount);
    }

    @Test
    void testDeletesAccount() {
        Account existing = new Account();
        existing.setId(99);
        existing.setUsername("testuser");

        when(accountRepository.findById(99)).thenReturn(Optional.of(existing));

        service.delete(99);

        verify(favoriteRepository).deleteByUsername("testuser");
        verify(accountRepository).delete(existing);
    }

}
