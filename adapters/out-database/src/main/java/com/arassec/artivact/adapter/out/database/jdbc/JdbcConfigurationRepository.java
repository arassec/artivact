package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.ConfigurationEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.ConfigurationEntityRepository;
import com.arassec.artivact.application.port.out.repository.ConfigurationRepository;
import com.arassec.artivact.domain.model.configuration.ConfigurationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

/**
 * {@link ConfigurationRepository} that uses JDBC to save configuration values.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcConfigurationRepository extends BaseJdbcRepository implements ConfigurationRepository {

    /**
     * Spring-Data's repository for {@link ConfigurationEntity}s.
     */
    private final ConfigurationEntityRepository configurationEntityRepository;

    /**
     * Jackson's ObjectMapper.
     */
    @Getter
    private final JsonMapper jsonMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Optional<T> findByType(ConfigurationType configurationType, Class<T> configurationClazz) {
        ConfigurationEntity configurationEntity = loadOrCreateEntity(configurationType.name());
        if (StringUtils.hasText(configurationEntity.getContentJson())) {
            return Optional.of(fromJson(configurationEntity.getContentJson(), configurationClazz));
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveConfiguration(ConfigurationType configurationType, Object configurationObject) {
        saveEntity(configurationType, toJson(configurationObject));
    }

    /**
     * Loads or creates a new configuration entity.
     *
     * @param id The ID of the configuration entity to load/create.
     * @return The entity.
     */
    private ConfigurationEntity loadOrCreateEntity(String id) {
        Optional<ConfigurationEntity> entityOptional = configurationEntityRepository.findById(id);

        ConfigurationEntity entity;
        if (entityOptional.isPresent()) {
            entity = entityOptional.get();
        } else {
            entity = new ConfigurationEntity();
            entity.setId(id);
        }

        return entity;
    }

    /**
     * Saves a configuration entity.
     *
     * @param configurationType The type of configuration.
     * @param contentJson       The content to save.
     */
    private void saveEntity(ConfigurationType configurationType, String contentJson) {
        ConfigurationEntity entity = loadOrCreateEntity(configurationType.name());
        entity.setContentJson(contentJson);
        configurationEntityRepository.save(entity);
    }

}
