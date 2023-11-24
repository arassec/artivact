package com.arassec.artivact.backend;

import com.arassec.artivact.backend.service.mapper.WidgetDeserializer;
import com.arassec.artivact.backend.service.model.Roles;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan
@EntityScan
@EnableWebSecurity
@EnableAspectJAutoProxy
@EnableJpaRepositories
@EnableTransactionManagement
public class ArtivactBackendConfiguration {

    private static final String API_ACCOUNT_OWN_PATTERN = "/api/account/own";

    private static final String API_ACCOUNT_PATTERN = "/api/account";

    private static final String API_CONFIGURATION_PUBLIC_PATTERN = "/api/configuration/public";

    private static final String API_CONFIGURATION_PATTERN = "/api/configuration";

    private static final String API_ITEM_PATTERN = "/api/item";

    private static final String API_PAGE_PATTERN = "/api/page";

    private static final String API_IMPORT_PATTERN = "/api/import";

    private static final String API_SEARCH_INDEX_PATTERN = "/api/search/index";

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
                        .requestMatchers(HttpMethod.POST, API_ITEM_PATTERN).hasRole(Roles.USER)
                        .requestMatchers(HttpMethod.PUT, API_ITEM_PATTERN).hasRole(Roles.USER)
                        .requestMatchers(HttpMethod.DELETE, API_ITEM_PATTERN).hasRole(Roles.USER)
                        .requestMatchers(HttpMethod.POST, API_PAGE_PATTERN).hasRole(Roles.USER)
                        .requestMatchers(HttpMethod.PUT, API_PAGE_PATTERN).hasRole(Roles.USER)
                        .requestMatchers(HttpMethod.DELETE, API_PAGE_PATTERN).hasRole(Roles.USER)
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

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        var mapperModule = new SimpleModule();

        mapperModule.addDeserializer(Widget.class, new WidgetDeserializer());

        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(mapperModule);
    }

}
