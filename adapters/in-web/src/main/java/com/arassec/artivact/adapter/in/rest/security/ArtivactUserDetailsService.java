package com.arassec.artivact.adapter.in.rest.security;

import com.arassec.artivact.application.port.in.account.LoadAccountUseCase;
import com.arassec.artivact.application.service.account.AccountService;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.account.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * An implementation of Spring's {@link UserDetailsService} that uses the application's {@link AccountService} to load users.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArtivactUserDetailsService implements UserDetailsService {

    private final LoadAccountUseCase loadAccountUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Account> optionalAccount = loadAccountUseCase.loadByUsername(username);

        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();

            Set<String> roles = new HashSet<>();
            if (Boolean.TRUE.equals(account.getAdmin())) {
                roles.add(Roles.ADMIN);
            }
            if (Boolean.TRUE.equals(account.getUser())) {
                roles.add(Roles.USER);
            }

            return User.builder()
                    .username(account.getUsername())
                    .password(account.getPassword())
                    .roles(roles.toArray(new String[0]))
                    .build();
        }

        throw new UsernameNotFoundException("No account with username " + username + " found!");
    }

}
