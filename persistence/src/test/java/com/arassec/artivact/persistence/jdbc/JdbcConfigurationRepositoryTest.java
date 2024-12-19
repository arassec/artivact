package com.arassec.artivact.persistence.jdbc;

import com.arassec.artivact.core.model.configuration.ConfigurationType;
import com.arassec.artivact.core.model.configuration.ExchangeConfiguration;
import com.arassec.artivact.persistence.jdbc.springdata.entity.ConfigurationEntity;
import com.arassec.artivact.persistence.jdbc.springdata.repository.ConfigurationEntityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcConfigurationRepository}.
 */
@ExtendWith(MockitoExtension.class)
class JdbcConfigurationRepositoryTest {

    /**
     * Repository under test.
     */
    @InjectMocks
    private JdbcConfigurationRepository jdbcConfigurationRepository;

    /**
     * Spring-Data configuration repository mock.
     */
    @Mock
    private ConfigurationEntityRepository configurationEntityRepository;

    /**
     * ObjectMapper mock.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Tests loading a configuration by its type which has not been configured before.
     */
    @Test
    void testFindByTypeNewConfiguration() {
        Optional<ExchangeConfiguration> exchangeConfigurationOptional =
                jdbcConfigurationRepository.findByType(ConfigurationType.EXCHANGE, ExchangeConfiguration.class);
        assertTrue(exchangeConfigurationOptional.isEmpty());
    }

    /**
     * Tests loading a configuration by its type.
     */
    @Test
    @SneakyThrows
    void testFindByType() {
        ConfigurationEntity configurationEntity = new ConfigurationEntity();
        configurationEntity.setContentJson("{contentJson}");

        when(configurationEntityRepository.findById("EXCHANGE")).thenReturn(Optional.of(configurationEntity));

        ExchangeConfiguration exchangeConfiguration = new ExchangeConfiguration();

        when(objectMapper.readValue("{contentJson}", ExchangeConfiguration.class)).thenReturn(
                exchangeConfiguration
        );

        Optional<ExchangeConfiguration> exchangeConfigurationOptional =
                jdbcConfigurationRepository.findByType(ConfigurationType.EXCHANGE, ExchangeConfiguration.class);

        assertTrue(exchangeConfigurationOptional.isPresent());
        assertEquals(exchangeConfiguration, exchangeConfigurationOptional.get());
    }

    /**
     * Tests saving a configuration.
     */
    @Test
    @SneakyThrows
    void testSaveConfiguration() {
        ConfigurationEntity configurationEntity = new ConfigurationEntity();

        when(configurationEntityRepository.findById("EXCHANGE")).thenReturn(Optional.of(configurationEntity));

        ExchangeConfiguration exchangeConfiguration = new ExchangeConfiguration();

        when(objectMapper.writeValueAsString(exchangeConfiguration)).thenReturn("{contentJson}");

        jdbcConfigurationRepository.saveConfiguration(ConfigurationType.EXCHANGE, exchangeConfiguration);

        verify(configurationEntityRepository, times(1)).save(configurationEntity);
        assertEquals("{contentJson}", configurationEntity.getContentJson());
    }

}
