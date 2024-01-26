package com.arassec.artivact.backend;

import com.arassec.artivact.backend.service.model.Roles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Profile("!desktop")
@Configuration
public class ArtivactServerSecurityConfiguration {


    private static final String API_ACCOUNT_OWN_PATTERN = "/api/account/own";

    private static final String API_ACCOUNT_PATTERN = "/api/account";

    private static final String API_CONFIGURATION_PUBLIC_PATTERN = "/api/configuration/public";

    private static final String API_CONFIGURATION_PATTERN = "/api/configuration";

    private static final String API_ITEM_PATTERN = "/api/item";

    private static final String API_PAGE_PATTERN = "/api/page";

    private static final String API_IMPORT_PATTERN = "/api/import";

    private static final String API_SEARCH_INDEX_PATTERN = "/api/search/index";

    private static final String API_MEDIA_CREATION_PATTERN = "/api/media-creation";

    private static final String API_EXHIBITION_PATTERN = "/api/exhibition";

    private static final String API_EXCHANGE_PATTERN = "/api/exchange";

    private static final String API_EXCHANGE_SYNC_PATTERN = "/api/exchange/sync";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, API_CONFIGURATION_PUBLIC_PATTERN).permitAll()
                        .requestMatchers(API_ACCOUNT_OWN_PATTERN).authenticated()
                        .requestMatchers(API_IMPORT_PATTERN).hasRole(Roles.ADMIN)
                        .requestMatchers(API_SEARCH_INDEX_PATTERN).hasRole(Roles.ADMIN)
                        .requestMatchers(API_ACCOUNT_PATTERN).hasRole(Roles.ADMIN)
                        .requestMatchers(API_CONFIGURATION_PATTERN).hasRole(Roles.ADMIN)
                        .requestMatchers(API_EXCHANGE_SYNC_PATTERN).permitAll()
                        .requestMatchers(API_EXCHANGE_PATTERN).hasRole(Roles.ADMIN)
                        .requestMatchers(API_MEDIA_CREATION_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(API_EXHIBITION_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.POST, API_ITEM_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.PUT, API_ITEM_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.DELETE, API_ITEM_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.POST, API_PAGE_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.PUT, API_PAGE_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.DELETE, API_PAGE_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/#/user-login")
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value()))
                        .failureHandler((request, response, exception) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()))
                        .permitAll()
                )
                .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value()))
                        .permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .build();
    }

}
