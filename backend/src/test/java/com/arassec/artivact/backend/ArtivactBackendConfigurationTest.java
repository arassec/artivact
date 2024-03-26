package com.arassec.artivact.backend;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.exhibition.Exhibition;
import com.arassec.artivact.backend.service.model.exhibition.ToolType;
import com.arassec.artivact.backend.service.model.exhibition.Topic;
import com.arassec.artivact.backend.service.model.exhibition.tool.TitleTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Tests the {@link ArtivactBackendConfiguration} functionality.
 */
@Slf4j
public class ArtivactBackendConfigurationTest {

    /**
     * The configuration under test.
     */
    ArtivactBackendConfiguration artivactBackendConfiguration = new ArtivactBackendConfiguration();

    /**
     * Tests the password encoder.
     */
    @Test
    void testEncoder() {
        assertInstanceOf(BCryptPasswordEncoder.class, artivactBackendConfiguration.encoder());
    }

    /**
     * Tests the object mapper configuration.
     */
    @Test
    @SneakyThrows
    void testObjectMapper() {
        ObjectMapper objectMapper = artivactBackendConfiguration.objectMapper();

        TranslatableString title = new TranslatableString();
        title.setValue("testObjectMapper");

        TitleTool tool = new TitleTool();
        tool.setTitle(title);

        Topic topic = new Topic();
        topic.setTitle(title);
        topic.getTools().add(tool);

        Exhibition exhibition = new Exhibition();
        exhibition.getTopics().add(topic);

        String serializedExhibition = objectMapper.writeValueAsString(exhibition);
        log.info("Serialized exhibition: {}", serializedExhibition);

        Exhibition deserializedExhibition = objectMapper.readValue(serializedExhibition, Exhibition.class);
        assertEquals(ToolType.TITLE, deserializedExhibition.getTopics().getFirst().getTools().getFirst().getType());
    }


}
