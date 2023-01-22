package com.arassec.artivact.vault.backend.service;

import com.arassec.artivact.vault.backend.core.ArtivactVaultException;
import com.arassec.artivact.vault.backend.service.model.TranslatableItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class BaseService {

    @Getter
    private final ObjectMapper objectMapper;

    protected String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ArtivactVaultException("Could not convert object to JSON-String!", e);
        }
    }

    protected <T> T fromJson(String json, Class<T> clazz) {
        if (!StringUtils.hasText(json)) {
            try {
                return clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new ArtivactVaultException("Could not create default instance of object!", e);
            }
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ArtivactVaultException("Could not convert JSON-String to object!", e);
        }
    }

    protected boolean isAllowed(Optional<? extends TranslatableItem> translatableItemOptional, List<String> roles) {
        if (translatableItemOptional.isEmpty() || translatableItemOptional.get().getRestrictions().isEmpty()) {
            return true;
        }
        return translatableItemOptional.get().getRestrictions().stream()
                .anyMatch(roles::contains);
    }

}
