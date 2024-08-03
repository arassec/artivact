package com.arassec.artivact.web.security;

import com.arassec.artivact.core.model.account.Account;
import com.arassec.artivact.domain.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ArtivactUserDetailsService}.
 */
@ExtendWith(MockitoExtension.class)
class ArtivactUserDetailsServiceTest {

    /**
     * The service under test.
     */
    @InjectMocks
    private ArtivactUserDetailsService artivactUserDetailsService;

    /**
     * Mock of the application's account service.
     */
    @Mock
    private AccountService accountService;

    /**
     * Tests loading an unknown user is failsafe.
     */
    @Test
    void testLoadUserByUsernameFail() {
        assertThrows(UsernameNotFoundException.class, () -> artivactUserDetailsService.loadUserByUsername("invalid"));
    }

    /**
     * Tests loading a user by name.
     */
    @Test
    void testLoadUserByUsername() {
        when(accountService.loadByUsername("username")).thenReturn(Optional.of(Account.builder()
                .username("username")
                .password("password")
                .user(true)
                .admin(true)
                .build()));

        UserDetails userDetails = artivactUserDetailsService.loadUserByUsername("username");

        assertEquals("username", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertEquals("[ROLE_ADMIN, ROLE_USER]", String.valueOf(userDetails.getAuthorities()));
    }

}
