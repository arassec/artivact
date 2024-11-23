package com.arassec.artivact.web.security;

import com.arassec.artivact.core.model.Roles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring-Security configuration for server-mode.
 */
@Profile("!desktop & !e2e")
@Configuration
public class ArtivactServerSecurityConfiguration {

    /**
     * API path for account management of the current user's account.
     */
    private static final String API_ACCOUNT_OWN_PATTERN = "/api/account/own";

    /**
     * API path for administration of accounts.
     */
    private static final String API_ACCOUNT_PATTERN = "/api/account";

    /**
     * API path for public configuration requests.
     */
    private static final String API_CONFIGURATION_PUBLIC_PATTERN = "/api/configuration/public";

    /**
     * API path for configuration administration.
     */
    private static final String API_CONFIGURATION_PATTERN = "/api/configuration";

    /**
     * API path for item requests.
     */
    private static final String API_ITEM_PATTERN = "/api/item";

    /**
     * API path for media-creation requests.
     */
    private static final String API_ITEM_MEDIA_CREATION_PATTERN = "/api/item/{itemId}/media-creation";

    /**
     * API path for page requests.
     */
    private static final String API_PAGE_PATTERN = "/api/page";

    /**
     * API path for import handling in server-to-server communication.
     */
    private static final String API_IMPORT_REMOTE_PATTERN = "/api/import/remote";

    /**
     * API path for import administration.
     */
    private static final String API_IMPORT_PATTERN = "/api/import";

    /**
     * API path for export handling.
     */
    private static final String API_EXPORT_PATTERN = "/api/export";

    /**
     * API path for search index administration.
     */
    private static final String API_SEARCH_INDEX_PATTERN = "/api/search/index";

    /**
     * API path for batch processing.
     */
    private static final String API_BATCH_PROCESS_PATTERN = "/api/batch";

    /**
     * Provides a security filter-chain for Spring-Security when the application is run in server-mode.
     *
     * @param http Spring-Security's {@link HttpSecurity}.
     * @return A server-mode {@link SecurityFilterChain}.
     * @throws Exception in case of errors.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, API_CONFIGURATION_PUBLIC_PATTERN).permitAll()
                        .requestMatchers(API_ACCOUNT_OWN_PATTERN).authenticated()
                        .requestMatchers(API_IMPORT_REMOTE_PATTERN).permitAll()
                        .requestMatchers(API_IMPORT_PATTERN).hasRole(Roles.ADMIN)
                        .requestMatchers(API_EXPORT_PATTERN).hasRole(Roles.ADMIN)
                        .requestMatchers(API_SEARCH_INDEX_PATTERN).hasRole(Roles.ADMIN)
                        .requestMatchers(API_ACCOUNT_PATTERN).hasRole(Roles.ADMIN)
                        .requestMatchers(API_CONFIGURATION_PATTERN).hasRole(Roles.ADMIN)
                        .requestMatchers(API_ITEM_MEDIA_CREATION_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.POST, API_ITEM_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.PUT, API_ITEM_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.DELETE, API_ITEM_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.POST, API_PAGE_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.PUT, API_PAGE_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.DELETE, API_PAGE_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
                        .requestMatchers(HttpMethod.POST, API_BATCH_PROCESS_PATTERN).hasAnyRole(Roles.ADMIN, Roles.USER)
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
