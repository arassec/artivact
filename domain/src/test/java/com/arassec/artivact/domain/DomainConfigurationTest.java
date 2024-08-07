package com.arassec.artivact.domain;

import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.page.WidgetType;
import com.arassec.artivact.core.model.page.widget.TextWidget;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Tests the {@link DomainConfiguration} functionality.
 */
@Slf4j
class DomainConfigurationTest {

    /**
     * The configuration under test.
     */
    DomainConfiguration domainConfiguration = new DomainConfiguration();

    /**
     * Tests the password encoder.
     */
    @Test
    void testEncoder() {
        assertInstanceOf(BCryptPasswordEncoder.class, domainConfiguration.encoder());
    }

    /**
     * Tests the object mapper configuration.
     */
    @Test
    @SneakyThrows
    void testObjectMapper() {
        ObjectMapper objectMapper = domainConfiguration.objectMapper();

        TranslatableString title = new TranslatableString();
        title.setValue("testObjectMapper");

        TextWidget textWidget = new TextWidget();
        textWidget.setId("123abc");
        textWidget.setRestrictions(Set.of("ROLE_ADMIN"));
        textWidget.setHeading(title);

        String serializedWidget = objectMapper.writeValueAsString(textWidget);
        log.info("Serialized widget: {}", serializedWidget);

        TextWidget deserializedWidget = objectMapper.readValue(serializedWidget, TextWidget.class);
        assertEquals(WidgetType.TEXT, deserializedWidget.getType());
        assertEquals("testObjectMapper", deserializedWidget.getHeading().getValue());
        assertEquals("123abc", deserializedWidget.getId());
        assertEquals("ROLE_ADMIN", deserializedWidget.getRestrictions().stream().findFirst().orElseThrow());
    }


}
