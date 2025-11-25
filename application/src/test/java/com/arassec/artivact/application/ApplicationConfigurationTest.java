package com.arassec.artivact.application;

import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.page.WidgetType;
import com.arassec.artivact.domain.model.page.widget.TextWidget;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Tests the {@link ApplicationConfiguration} functionality.
 */
@Slf4j
class ApplicationConfigurationTest {

    /**
     * The configuration under test.
     */
    ApplicationConfiguration domainConfiguration = new ApplicationConfiguration();

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
    void testJsonMapper() {
        JsonMapper jsonMapper = domainConfiguration.jsonMapper();

        TranslatableString title = new TranslatableString();
        title.setValue("testObjectMapper");

        TextWidget textWidget = new TextWidget();
        textWidget.setId("123abc");
        textWidget.setRestrictions(Set.of("ROLE_ADMIN"));
        textWidget.setHeading(title);

        String serializedWidget = jsonMapper.writeValueAsString(textWidget);
        log.info("Serialized widget: {}", serializedWidget);

        TextWidget deserializedWidget = jsonMapper.readValue(serializedWidget, TextWidget.class);
        assertEquals(WidgetType.TEXT, deserializedWidget.getType());
        assertEquals("testObjectMapper", deserializedWidget.getHeading().getValue());
        assertEquals("123abc", deserializedWidget.getId());
        assertEquals("ROLE_ADMIN", deserializedWidget.getRestrictions().stream().findFirst().orElseThrow());
    }


}
