package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.service.exception.VaultException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;

public abstract class BaseService {

    protected abstract ObjectMapper getObjectMapper();

    protected String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new VaultException("Could not convert object to JSON-String!", e);
        }
    }

    protected <T> T fromJson(String json, Class<T> clazz) {
        if (!StringUtils.hasText(json)) {
            try {
                return clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new VaultException("Could not create default instance of object!", e);
            }
        }
        try {
            return getObjectMapper().readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new VaultException("Could not convert JSON-String to object!", e);
        }
    }

}
