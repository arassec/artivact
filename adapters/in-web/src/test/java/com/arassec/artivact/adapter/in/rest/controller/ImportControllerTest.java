package com.arassec.artivact.adapter.in.rest.controller;

import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.exchange.ImportContentUseCase;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ImportController}.
 */
@ExtendWith(MockitoExtension.class)
class ImportControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private ImportController controller;

    @Mock
    private ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;

    @Mock
    private ImportTagsConfigurationUseCase importTagsConfigurationUseCase;

    /**
     * The application's {@link ImportContentUseCase}.
     */
    @Mock
    private ImportContentUseCase importContentUseCase;

    /**
     * The object mapper for exports.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Tests importing previously exported properties.
     */
    @Test
    @SneakyThrows
    void testImportPropertiesConfiguration() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn("properties-config-json".getBytes());

        ResponseEntity<String> stringResponseEntity = controller.importPropertiesConfiguration(file);

        assertEquals("Properties imported.", stringResponseEntity.getBody());
        verify(importPropertiesConfigurationUseCase, times(1)).importPropertiesConfiguration("properties-config-json");
    }

    /**
     * Tests importing previously exported tags.
     */
    @Test
    @SneakyThrows
    void testImportTagsConfiguration() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn("tags-config-json".getBytes());

        ResponseEntity<String> stringResponseEntity = controller.importTagsConfiguration(file);

        assertEquals("Tags imported.", stringResponseEntity.getBody());
        verify(importTagsConfigurationUseCase, times(1)).importTagsConfiguration("tags-config-json");
    }

    /**
     * Tests importing a menu.
     */
    @Test
    void testImportMenu() {
        MultipartFile file = mock(MultipartFile.class);
        ResponseEntity<String> stringResponseEntity = controller.importMenu(file);
        assertThat(stringResponseEntity.getBody()).isEqualTo("Menu imported.");
        verify(importContentUseCase).importContent(any(Path.class));
    }

    /**
     * Test importing an item.
     */
    @Test
    void testImportItem() {
        MultipartFile file = mock(MultipartFile.class);
        ResponseEntity<String> stringResponseEntity = controller.importItem(file);
        assertThat(stringResponseEntity.getBody()).isEqualTo("Item imported.");
        verify(importContentUseCase).importContent(any(Path.class));
    }

    /**
     * Tests importing an item using an API token.
     */
    @Test
    void testImportItemWithApiToken() {
        MultipartFile file = mock(MultipartFile.class);
        ResponseEntity<String> stringResponseEntity = controller.importItemWithApiToken(file, "123-abc");
        assertThat(stringResponseEntity.getBody()).isEqualTo("Item synchronized.");
        verify(importContentUseCase).importContent(any(Path.class), eq("123-abc"));
    }

}
