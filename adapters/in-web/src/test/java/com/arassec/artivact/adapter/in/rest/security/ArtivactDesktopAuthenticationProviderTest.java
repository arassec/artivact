package com.arassec.artivact.adapter.in.rest.security;

import com.arassec.artivact.application.service.account.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.jaas.JaasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ArtivactDesktopAuthenticationProvider}.
 */
@ExtendWith(MockitoExtension.class)
class ArtivactDesktopAuthenticationProviderTest {

    /**
     * The class under test.
     */
    @InjectMocks
    private ArtivactDesktopAuthenticationProvider artivactDesktopAuthenticationProvider;

    /**
     * The application's {@link AccountService}.
     */
    @Mock
    private ArtivactUserDetailsService artivactUserDetailsService;

    /**
     * Tests authentication in desktop mode.
     */
    @Test
    void testAuthenticate() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getPassword()).thenReturn("encoded-password");

        when(artivactUserDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

        Authentication result = artivactDesktopAuthenticationProvider.authenticate(mock(Authentication.class));

        assertEquals("encoded-password", result.getCredentials());
    }

    /**
     * Tests supported authentication mechanisms.
     */
    @Test
    void testSupports() {
        assertTrue(artivactDesktopAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
        assertFalse(artivactDesktopAuthenticationProvider.supports(JaasAuthenticationToken.class));
    }

}
