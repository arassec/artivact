package com.arassec.artivact.backend.api.security;

import com.arassec.artivact.backend.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Profile("desktop")
@Component
@RequiredArgsConstructor
public class ArtivactDesktopAuthenticationProvider implements AuthenticationProvider {

    private final AccountService accountService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        UserDetails admin = accountService.loadUserByUsername("admin");

        return new UsernamePasswordAuthenticationToken
                (admin, admin.getPassword(), admin.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}