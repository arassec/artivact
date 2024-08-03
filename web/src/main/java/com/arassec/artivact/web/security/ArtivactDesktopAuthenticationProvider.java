package com.arassec.artivact.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Custom authentication provider if the application is run in desktop mode.
 * <p>
 * This is required to auto-login users in desktop mode, i.e. disable user management completely.
 */
@Slf4j
@Profile({"desktop", "e2e"})
@Component
@RequiredArgsConstructor
public class ArtivactDesktopAuthenticationProvider implements AuthenticationProvider {

    /**
     * The application's {@link ArtivactUserDetailsService}.
     */
    private final ArtivactUserDetailsService artivactDesktopUserDetailsService;

    /**
     * Always authenticates the user as application administrator.
     *
     * @param authentication The Spring-Security {@link Authentication} object.
     * @return A newly created {@link UsernamePasswordAuthenticationToken} with the administrator account.
     * @throws AuthenticationException in case of failures.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        log.info("Auto-Authenticating user in Desktop-Mode as admin!");

        UserDetails admin = artivactDesktopUserDetailsService.loadUserByUsername("admin");

        return new UsernamePasswordAuthenticationToken
                (admin, admin.getPassword(), admin.getAuthorities());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
