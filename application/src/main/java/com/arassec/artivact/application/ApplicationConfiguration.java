package com.arassec.artivact.application;

import com.arassec.artivact.application.infrastructure.mapping.PeripheralConfigDeserializer;
import com.arassec.artivact.application.infrastructure.mapping.WidgetDeserializer;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

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
    public JsonMapper jsonMapper() {
        var artivactMapperModule = new SimpleModule("ArtivactMapperModule");
        artivactMapperModule.addDeserializer(Widget.class, new WidgetDeserializer());
        artivactMapperModule.addDeserializer(PeripheralConfig.class, new PeripheralConfigDeserializer());

        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .addModule(artivactMapperModule)
                .build();
    }

    @Bean("backgroundOperationExecutorService")
    @ConditionalOnMissingBean(ExecutorService.class)
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(1);
    }

}
