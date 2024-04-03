package com.arassec.artivact.starter;

import com.arassec.artivact.backend.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests the {@link ArtivactAutoConfiguration}.
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = ArtivactAutoConfiguration.class)
class ArtivactAutoConfigurationTest {

    /**
     * The spring application context.
     */
    @Autowired
    private ApplicationContext context;

    /**
     * Simply tests Spring context creation.
     */
    @Test
    void testContextCreation() {
        assertNotNull(context.getBean(ItemService.class));
    }

}
