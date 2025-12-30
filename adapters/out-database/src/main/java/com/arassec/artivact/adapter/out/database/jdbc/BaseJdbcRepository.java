package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.domain.exception.ArtivactException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.InvocationTargetException;

/**
 * Repository for base jdbc.
 */
public abstract class BaseJdbcRepository {

    /**
     * Returns the object mapper to use.
     *
     * @return The application's primary {@link ObjectMapper}.
     */
    protected abstract JsonMapper getJsonMapper();

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
        return getJsonMapper().writeValueAsString(object);
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
        return getJsonMapper().readValue(json, clazz);

    }

}
