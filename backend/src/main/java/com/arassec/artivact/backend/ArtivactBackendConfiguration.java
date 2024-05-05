package com.arassec.artivact.backend;

import com.arassec.artivact.backend.service.mapper.WidgetDeserializer;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring configuration of the Artivact backend application.
 */
@Configuration
@ComponentScan
@EntityScan
@EnableWebSecurity
@EnableAspectJAutoProxy
@EnableJpaRepositories
@EnableTransactionManagement
public class ArtivactBackendConfiguration {

    /**
     * Password encoder to ues.
     *
     * @return The system's password encoder.
     */
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Object mapper that ignores unknown properties.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        var artivactMapperModule = new SimpleModule();
        artivactMapperModule.addDeserializer(Widget.class, new WidgetDeserializer());

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(artivactMapperModule);
        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper;
    }

}
