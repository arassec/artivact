package com.arassec.artivact.server;

import com.arassec.artivact.application.service.configuration.ConfigurationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests the {@link ArtivactServerApplication}.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ArtivactServerApplicationTest {

    /**
     * The spring application context.
     */
    @Autowired
    private ApplicationContext context;

    /**
     * Simply tests Spring context creation.
     * <p>
     * Occurring JavaFX exceptions can be ignored, since the test doesn't create this environment.
     */
    @Test
    void testContextCreation() {
        assertNotNull(context.getBean(ConfigurationService.class));
    }

}
