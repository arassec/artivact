package com.arassec.artivact.application.infrastructure.aspect;

import com.arassec.artivact.application.service.DefaultLocaleProvider;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.TranslatableObject;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * Implements the {@link TranslateResult} aspect.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class TranslateResultAspect {

    /**
     * Only classes from this package (and sub-packages) are processed.
     */
    private static final String ARTIVACT_PACKAGE_PREFIX = "com.arassec.artivact";

    /**
     * Repository for loading the application's configuration.
     */
    private final DefaultLocaleProvider defaultLocaleProvider;

    /**
     * Processes a method's result value and translates it if required.
     *
     * @param joinPoint The aspectj {@link ProceedingJoinPoint}.
     * @return The translated object.
     * @throws Throwable In case of errors.
     */
    @Around("@annotation(com.arassec.artivact.application.infrastructure.aspect.TranslateResult)")
    public Object translate(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        String defaultLocale = defaultLocaleProvider.getDefaultLocale();

        translateIfPossible(result, LocaleContextHolder.getLocale(), defaultLocale);
        return result;
    }

    /**
     * Translates the given object if required with the given locale.
     *
     * @param object        The object to translate.
     * @param locale        The locale to use for translation.
     * @param defaultLocale The default locale to use as fallback.
     */
    private void translateIfPossible(Object object, Locale locale, String defaultLocale) {
        if (object == null) {
            return;
        }
        switch (object) {
            case TranslatableObject translatableObject -> {
                translatableObject.translate(locale.toString(), defaultLocale);
                translatePropertiesIfPossible(translatableObject, locale, defaultLocale);
            }
            case Collection<?> collectionToTranslate ->
                    collectionToTranslate.forEach(entry -> translateIfPossible(entry, locale, defaultLocale));
            default -> translatePropertiesIfPossible(object, locale, defaultLocale);
        }
    }

    /**
     * Translates an object's properties if required.
     *
     * @param object        The object to process.
     * @param locale        The locale to use for translation.
     * @param defaultLocale The default locale to use as fallback.
     */
    @SuppressWarnings("java:S3011") // declaredField.setAccessible(true) is intentional here!
    private void translatePropertiesIfPossible(Object object, Locale locale, String defaultLocale) {
        if (object == null) {
            return;
        }
        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                if (field.getType().getName().startsWith(ARTIVACT_PACKAGE_PREFIX) && !field.getType().isEnum()) {
                    field.setAccessible(true);
                    Object declaredFieldValue = field.get(object);
                    translateIfPossible(declaredFieldValue, locale, defaultLocale);
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Collection<?> collection = (Collection<?>) field.get(object);
                    collection.forEach(collectionEntry -> translateIfPossible(collectionEntry, locale, defaultLocale));
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Map<?, ?> map = (Map<?, ?>) field.get(object);
                    map.forEach((key, value) -> {
                        translateIfPossible(key, locale, defaultLocale);
                        translateIfPossible(value, locale, defaultLocale);
                    });
                }
            } catch (IllegalAccessException e) {
                throw new ArtivactException("Could not translate method result!", e);
            }
        }
    }

}
