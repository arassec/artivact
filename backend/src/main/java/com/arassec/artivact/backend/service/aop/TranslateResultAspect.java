package com.arassec.artivact.backend.service.aop;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.TranslatableItem;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Locale;

@Aspect
@Component
public class TranslateResultAspect {

    private static final String ARTIVACT_PACKAGE_PREFIX = "com.arassec.artivact";

    @Around("@annotation(com.arassec.artivact.backend.service.aop.TranslateResult)")
    public Object translate(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        translateIfPossible(result, LocaleContextHolder.getLocale());
        return result;
    }

    private void translateIfPossible(Object object, Locale locale) {
        if (object instanceof TranslatableItem translatableItem) {
            translatableItem.translate(locale);
            translatePropertiesIfPossible(translatableItem, locale);
        } else if (object instanceof Collection<?> collectionToTranslate) {
            collectionToTranslate.forEach(entry -> translateIfPossible(entry, locale));
        } else {
            translatePropertiesIfPossible(object, locale);
        }
    }

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
