package com.arassec.artivact.backend.service.aop;

import com.arassec.artivact.backend.service.exception.VaultException;
import com.arassec.artivact.backend.service.model.IdentifiedItem;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;

@Aspect
@Component
public class GenerateIdsAspect {

    private static final String ARTIVACT_PACKAGE_PREFIX = "com.arassec.artivact";


    @Before("@annotation(com.arassec.artivact.backend.service.aop.GenerateIds)")
    public void generateIds(JoinPoint joinPoint) {
        for (Object argument : joinPoint.getArgs()) {
            generateIdsIfRequired(argument);
        }
    }

    private void generateIdsIfRequired(Object object) {
        if (object instanceof IdentifiedItem identifiedItem && !StringUtils.hasText(identifiedItem.getId())) {
            identifiedItem.setId(UUID.randomUUID().toString());
            generateIdsOfPropertiesIfRequired(object);
        } else if (object instanceof Collection<?> collection) {
            collection.forEach(this::generateIdsIfRequired);
        } else {
            generateIdsOfPropertiesIfRequired(object);
        }
    }

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
                throw new VaultException("Could not generate IDs for IdentifiedItem!", e);
            }
        }
    }

}
