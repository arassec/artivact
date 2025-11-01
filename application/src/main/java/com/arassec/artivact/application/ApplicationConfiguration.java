package com.arassec.artivact.application;

import com.arassec.artivact.application.infrastructure.mapper.PeripheralConfigDeserializer;
import com.arassec.artivact.application.infrastructure.mapper.WidgetDeserializer;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Spring configuration of the Artivact backend application.
 */
@Configuration
@ComponentScan
@EnableAspectJAutoProxy
public class ApplicationConfiguration {

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
        artivactMapperModule.addDeserializer(PeripheralConfig.class, new PeripheralConfigDeserializer());

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(artivactMapperModule);
        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper;
    }

    @Bean("backgroundOperationExecutorService")
    @ConditionalOnMissingBean(ExecutorService.class)
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(1);
    }

}
