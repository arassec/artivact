package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.AccountService;
import com.arassec.artivact.backend.service.model.account.Account;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link AccountController}.
 */
@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private AccountController controller;

    /**
     * Service mock.
     */
    @Mock
    private AccountService accountService;

    /**
     * Tests loading the current user's account.
     */
    @Test
    @SneakyThrows
    void testGetOwnAccount() {
        assertNull(controller.getOwnAccount(null));

        Account account = new Account();
        lenient().when(accountService.loadOwnAccount(any())).thenReturn(Optional.of(account));

        Account ownAccount = controller.getOwnAccount(new TestingAuthenticationToken(mock(UserDetails.class), "secret"));
        assertEquals(account, ownAccount);
    }

    /**
     * Tests updating the current user's account.
     */
    @Test
    void testUpdateOwnAccount() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("currentUsername");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        Account account = new Account();

        assertNull(controller.updateOwnAccount(null, null));
        assertNull(controller.updateOwnAccount(null, authentication));
        assertNull(controller.updateOwnAccount(account, null));

        when(accountService.updateOwnAccount("currentUsername", account)).thenReturn(account);

        assertEquals(account, controller.updateOwnAccount(account, authentication));
    }

    /**
     * Tests loading accounts.
     */
    @Test
    void testGetAccounts() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("username");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(accountService.loadAllExcept("username")).thenReturn(List.of());

        assertNotNull(controller.getAccounts(authentication));
    }

    /**
     * Tests creating an account.
     */
    @Test
    void testCreateAccount() {
        Account account = new Account();
        controller.createAccount(account);
        verify(accountService, times(1)).create(account);
    }

    /**
     * Tests updating an account.
     */
    @Test
    void testUpdateAccount() {
        Account account = new Account();
        controller.updateAccount(account);
        verify(accountService, times(1)).update(account);
    }

    /**
     * Tests deleting an account.
     */
    @Test
    void testDeleteAccount() {
        controller.deleteAccount(123);
        verify(accountService, times(1)).delete(123);
    }

}
