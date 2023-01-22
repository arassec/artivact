package com.arassec.artivact.vault.backend.service;

import com.arassec.artivact.vault.backend.core.ArtivactVaultException;
import com.arassec.artivact.vault.backend.persistence.model.ConfigurationEntity;
import com.arassec.artivact.vault.backend.persistence.repository.ConfigurationEntityRepository;
import com.arassec.artivact.vault.backend.service.model.Property;
import com.arassec.artivact.vault.backend.service.model.PropertyCategory;
import com.arassec.artivact.vault.backend.service.model.TranslatableItem;
import com.arassec.artivact.vault.backend.service.model.configuration.ConfigurationType;
import com.arassec.artivact.vault.backend.service.model.configuration.LicenseConfiguration;
import com.arassec.artivact.vault.backend.service.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.vault.backend.service.model.configuration.TagsConfiguration;
import com.arassec.artivact.vault.backend.web.model.TranslatedItem;
import com.arassec.artivact.vault.backend.web.model.TranslatedProperty;
import com.arassec.artivact.vault.backend.web.model.TranslatedPropertyCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ConfigurationService extends BaseService {

    private final ConfigurationEntityRepository configurationEntityRepository;

    public ConfigurationService(ObjectMapper objectMapper, ConfigurationEntityRepository configurationEntityRepository) {
        super(objectMapper);
        this.configurationEntityRepository = configurationEntityRepository;
    }

    public PropertiesConfiguration loadPropertiesConfiguration(List<String> roles) {
        ConfigurationEntity entity = loadEntity(ConfigurationType.PROPERTIES.name());

        if (StringUtils.hasText(entity.getContent())) {
            try {
                PropertiesConfiguration propertiesConfiguration = getObjectMapper().readValue(entity.getContent(),
                        PropertiesConfiguration.class);

                PropertiesConfiguration result = new PropertiesConfiguration();

                result.setCategories(propertiesConfiguration.getCategories().stream()
                        .filter(category -> isAllowed(Optional.of(category), roles))
                        .toList());

                result.getCategories().forEach(category -> category.getProperties().removeAll(
                        category.getProperties().stream()
                                .filter(property -> !isAllowed(Optional.of(property), roles))
                                .toList())
                );

                return result;
            } catch (JsonProcessingException e) {
                throw new ArtivactVaultException("Could not convert properties JSON!", e);
            }
        }

        return new PropertiesConfiguration();
    }

    public void savePropertiesConfiguration(PropertiesConfiguration propertiesConfiguration) {
        propertiesConfiguration.getCategories().forEach(propertyCategory -> {
            if (!StringUtils.hasText(propertyCategory.getId())) {
                propertyCategory.setId(UUID.randomUUID().toString());
            }
            propertyCategory.getProperties().forEach(property -> {
                if (!StringUtils.hasText(property.getId())) {
                    property.setId(UUID.randomUUID().toString());
                }
            });
        });

        ConfigurationEntity entity = loadEntity(ConfigurationType.PROPERTIES.name());

        try {
            entity.setContent(getObjectMapper().writeValueAsString(propertiesConfiguration));
        } catch (JsonProcessingException e) {
            throw new ArtivactVaultException("Could not convert properties configuration to JSON!", e);
        }

        configurationEntityRepository.save(entity);
    }

    public List<TranslatedPropertyCategory> loadTranslatedPropertyCategories(Locale locale, List<String> roles) {
        PropertiesConfiguration propertiesConfiguration = loadPropertiesConfiguration(roles);

        return propertiesConfiguration.getCategories().stream()
                .map(propertyCategory -> translatePropertyCategory(propertyCategory, locale))
                .toList();
    }

    public LicenseConfiguration loadLicenseConfiguration(List<String> roles) {
        ConfigurationEntity entity = loadEntity(ConfigurationType.LICENSE.name());

        if (StringUtils.hasText(entity.getContent())) {
            try {
                LicenseConfiguration licenseConfiguration = getObjectMapper().readValue(entity.getContent(),
                        LicenseConfiguration.class);

                LicenseConfiguration result = new LicenseConfiguration();

                if (isAllowed(Optional.ofNullable(licenseConfiguration.getPrefix()), roles)) {
                    result.setPrefix(licenseConfiguration.getPrefix());
                }

                if (isAllowed(Optional.ofNullable(licenseConfiguration.getLicenseLabel()), roles)) {
                    result.setLicenseLabel(licenseConfiguration.getLicenseLabel());
                }

                if (isAllowed(Optional.ofNullable(licenseConfiguration.getSuffix()), roles)) {
                    result.setSuffix(licenseConfiguration.getSuffix());
                }

                result.setLicenseUrl(licenseConfiguration.getLicenseUrl());

                return result;
            } catch (JsonProcessingException e) {
                throw new ArtivactVaultException("Could not convert license JSON to object!", e);
            }
        }

        LicenseConfiguration emptyConfiguration = new LicenseConfiguration();
        emptyConfiguration.setPrefix(new TranslatableItem());
        emptyConfiguration.setLicenseLabel(new TranslatableItem());
        emptyConfiguration.setSuffix(new TranslatableItem());

        return emptyConfiguration;
    }

    public void saveLicenseConfiguration(LicenseConfiguration licenseConfiguration) {
        if (licenseConfiguration.getPrefix() != null
                && !StringUtils.hasText(licenseConfiguration.getPrefix().getId())) {
            licenseConfiguration.getPrefix().setId(UUID.randomUUID().toString());
        }
        if (licenseConfiguration.getSuffix() != null
                && !StringUtils.hasText(licenseConfiguration.getSuffix().getId())) {
            licenseConfiguration.getSuffix().setId(UUID.randomUUID().toString());
        }
        if (licenseConfiguration.getLicenseLabel() != null
                && !StringUtils.hasText(licenseConfiguration.getLicenseLabel().getId())) {
            licenseConfiguration.getLicenseLabel().setId(UUID.randomUUID().toString());
        }

        ConfigurationEntity entity = loadEntity(ConfigurationType.LICENSE.name());

        try {
            entity.setContent(getObjectMapper().writeValueAsString(licenseConfiguration));
        } catch (JsonProcessingException e) {
            throw new ArtivactVaultException("Could not convert license configuration to JSON!", e);
        }

        configurationEntityRepository.save(entity);
    }

    public TagsConfiguration loadTagsConfiguration(List<String> roles) {
        ConfigurationEntity entity = loadEntity(ConfigurationType.TAGS.name());

        if (StringUtils.hasText(entity.getContent())) {
            try {
                TagsConfiguration tagsConfiguration = getObjectMapper().readValue(entity.getContent(),
                        TagsConfiguration.class);

                TagsConfiguration result = new TagsConfiguration();
                result.setTags(tagsConfiguration.getTags().stream()
                        .filter(tag -> isAllowed(Optional.of(tag), roles))
                        .toList());

                return result;
            } catch (JsonProcessingException e) {
                throw new ArtivactVaultException("Could not convert tags JSON to object!", e);
            }
        }

        return new TagsConfiguration();
    }

    public void saveTagsConfiguration(TagsConfiguration tagsConfiguration) {
        ConfigurationEntity entity = loadEntity(ConfigurationType.TAGS.name());

        tagsConfiguration.getTags().forEach(tag -> {
            if (!StringUtils.hasText(tag.getId())) {
                tag.setId(UUID.randomUUID().toString());
            }
        });

        try {
            entity.setContent(getObjectMapper().writeValueAsString(tagsConfiguration));
        } catch (JsonProcessingException e) {
            throw new ArtivactVaultException("Could not convert license configuration to JSON!", e);
        }

        configurationEntityRepository.save(entity);
    }

    private ConfigurationEntity loadEntity(String id) {
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

    private TranslatedPropertyCategory translatePropertyCategory(PropertyCategory propertyCategory, Locale locale) {
        TranslatedPropertyCategory result = new TranslatedPropertyCategory();
        translate(result, propertyCategory, locale);
        result.setProperties(propertyCategory.getProperties().stream()
                .map(property -> translatedProperty(property, locale))
                .toList()
        );
        return result;
    }

    private TranslatedProperty translatedProperty(Property property, Locale locale) {
        TranslatedProperty result = new TranslatedProperty();
        translate(result, property, locale);
        result.setValueRange(property.getValueRange().stream().
                map(valueRange -> {
                    TranslatedItem translatedItem = new TranslatedItem();
                    translate(translatedItem, valueRange, locale);
                    return translatedItem;
                })
                .toList());
        return result;
    }

    private void translate(TranslatedItem target, TranslatableItem translatableItem, Locale locale) {
        target.setId(translatableItem.getId());
        target.setValue(translatableItem.getValue());
        target.setTranslatedValue(translatableItem.getTranslatedValue(locale.toString()));
        target.setTranslations(translatableItem.getTranslations());
        target.setRestrictions(translatableItem.getRestrictions());
    }
}
