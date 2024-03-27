package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.model.configuration.PropertiesConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ExchangeController}.
 */
@ExtendWith(MockitoExtension.class)
public class ExchangeControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private ExchangeController exchangeController;

    /**
     * The application's {@link ConfigurationService}.
     */
    @Mock
    private ConfigurationService configurationService;

    /**
     * The object mapper for exports.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Tests exporting the current properties configuration.
     */
    @Test
    @SneakyThrows
    void testExportPropertiesConfiguration() {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        when(configurationService.loadPropertiesConfiguration()).thenReturn(propertiesConfiguration);

        when(objectMapper.writeValueAsString(propertiesConfiguration)).thenReturn("properties-configuration-json");

        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

        ResponseEntity<StreamingResponseBody> streamingResponseBodyResponseEntity =
                exchangeController.exportPropertiesConfiguration(httpServletResponse);

        assertEquals(HttpStatus.OK, streamingResponseBodyResponseEntity.getStatusCode());

        verify(httpServletResponse, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(httpServletResponse, times(1)).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.EXPIRES, "0");
    }

}
