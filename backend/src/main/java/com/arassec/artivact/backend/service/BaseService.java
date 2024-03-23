package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Base for services with JSON handling.
 */
public abstract class BaseService {

    /**
     * Returns the object mapper to use.
     *
     * @return The application's primary {@link ObjectMapper}.
     */
    protected abstract ObjectMapper getObjectMapper();

    /**
     * Serializes the given object to JSON.
     *
     * @param object The object to serialize.
     * @return The JSON representation of the given object.
     */
    protected String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not convert object to JSON-String!", e);
        }
    }

    /**
     * Deserializes the given object from JSON.
     *
     * @param json  The JSON string to deserialize.
     * @param clazz The desired target class.
     * @param <T>   The type of the target class.
     * @return The deserialized object.
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        if (!StringUtils.hasText(json)) {
            try {
                return clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new ArtivactException("Could not create default instance of object!", e);
            }
        }
        try {
            return getObjectMapper().readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not convert JSON-String to object!", e);
        }
    }

}
