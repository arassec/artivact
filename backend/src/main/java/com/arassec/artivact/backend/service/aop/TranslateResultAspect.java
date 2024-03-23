package com.arassec.artivact.backend.service.aop;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.TranslatableObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Locale;

/**
 * Implements the {@link TranslateResult} aspect.
 */
@Aspect
@Component
public class TranslateResultAspect {

    /**
     * Only classes from this package (and sub-packages) are processed.
     */
    private static final String ARTIVACT_PACKAGE_PREFIX = "com.arassec.artivact";

    /**
     * Processes a method's result value and translates it if required.
     *
     * @param joinPoint The aspectj {@link ProceedingJoinPoint}.
     * @return The translated object.
     * @throws Throwable In case of errors.
     */
    @Around("@annotation(com.arassec.artivact.backend.service.aop.TranslateResult)")
    public Object translate(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        translateIfPossible(result, LocaleContextHolder.getLocale());
        return result;
    }

    /**
     * Translates the given object if required with the given locale.
     *
     * @param object The object to translate.
     * @param locale The locale to use for translation.
     */
    private void translateIfPossible(Object object, Locale locale) {
        if (object instanceof TranslatableObject translatableObject) {
            translatableObject.translate(locale);
            translatePropertiesIfPossible(translatableObject, locale);
        } else if (object instanceof Collection<?> collectionToTranslate) {
            collectionToTranslate.forEach(entry -> translateIfPossible(entry, locale));
        } else {
            translatePropertiesIfPossible(object, locale);
        }
    }

    /**
     * Translates an object's properties if required.
     *
     * @param object The object to process.
     * @param locale The locale to use for translation.
     */
    @SuppressWarnings("java:S3011") // declaredField.setAccessible(true) is intentional here!
    private void translatePropertiesIfPossible(Object object, Locale locale) {
        if (object == null) {
            return;
        }
        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                if (field.getType().getName().startsWith(ARTIVACT_PACKAGE_PREFIX)) {
                    field.setAccessible(true);
                    Object declaredFieldValue = field.get(object);
                    translateIfPossible(declaredFieldValue, locale);
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Collection<?> collection = (Collection<?>) field.get(object);
                    collection.forEach(collectionEntry -> translateIfPossible(collectionEntry, locale));
                }
            } catch (IllegalAccessException e) {
                throw new ArtivactException("Could not translate method result!", e);
            }
        }
    }

}
