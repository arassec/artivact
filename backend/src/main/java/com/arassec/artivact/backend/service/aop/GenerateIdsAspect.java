package com.arassec.artivact.backend.service.aop;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.IdentifiedObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;

/**
 * Implements the {@link GenerateIds} aspect.
 */
@Aspect
@Component
public class GenerateIdsAspect {

    /**
     * Only classes from this package (and sub-packages) are processed.
     */
    private static final String ARTIVACT_PACKAGE_PREFIX = "com.arassec.artivact";

    /**
     * Recursively iterates the parameter object and generates UUID-IDs if necessary.
     *
     * @param joinPoint The aspectj {@link JoinPoint}.
     */
    @Before("@annotation(com.arassec.artivact.backend.service.aop.GenerateIds)")
    public void generateIds(JoinPoint joinPoint) {
        for (Object argument : joinPoint.getArgs()) {
            generateIdsIfRequired(argument);
        }
    }

    /**
     * Generates IDs if required on the target object.
     *
     * @param object The object to generate IDs for.
     */
    private void generateIdsIfRequired(Object object) {
        if (object == null) {
            return;
        }
        switch (object) {
            case IdentifiedObject identifiedObject when !StringUtils.hasText(identifiedObject.getId()) -> {
                identifiedObject.setId(UUID.randomUUID().toString());
                generateIdsOfPropertiesIfRequired(object);
            }
            case Collection<?> collection -> collection.forEach(this::generateIdsIfRequired);
            default -> generateIdsOfPropertiesIfRequired(object);
        }
    }

    /**
     * Generates IDs on an object's properties.
     *
     * @param object The object to process.
     */
    @SuppressWarnings("java:S3011") // declaredField.setAccessible(true) is intentional here!
    private void generateIdsOfPropertiesIfRequired(Object object) {
        if (object == null) {
            return;
        }
        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                if (field.getType().getName().startsWith(ARTIVACT_PACKAGE_PREFIX)) {
                    field.setAccessible(true);
                    Object declaredFieldValue = field.get(object);
                    generateIdsIfRequired(declaredFieldValue);
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Collection<?> collection = (Collection<?>) field.get(object);
                    collection.forEach(this::generateIdsIfRequired);
                }
            } catch (IllegalAccessException e) {
                throw new ArtivactException("Could not generate IDs for IdentifiedItem!", e);
            }
        }
    }

}
