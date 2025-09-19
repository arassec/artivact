package com.arassec.artivact.application.infrastructure.aspect;

import com.arassec.artivact.domain.model.TranslatableObject;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslateResultAspectTest {

    private final TranslateResultAspect aspect = new TranslateResultAspect();

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Getter
    static class DummyTranslatable implements TranslatableObject {
        boolean translated = false;

        @Override
        public void translate(String locale) {
            translated = true;
        }

        @Override
        public void translate(Locale locale) {
            translated = true;
        }

        @Override
        public void clear() {
            // Nothing to do here.
        }

    }

    static class NestedEntity {
        DummyTranslatable child;
    }

    static class MapEntity {
        Map<Object, Object> values = new HashMap<>();
    }

    @Test
    void testTranslateTranslatesSingleTranslatableObject() throws Throwable {
        DummyTranslatable obj = new DummyTranslatable();
        when(joinPoint.proceed()).thenReturn(obj);

        Object result = aspect.translate(joinPoint);

        assertThat(result).isEqualTo(obj);
        assertThat(obj.isTranslated()).isTrue();
    }

    @Test
    void testTranslateTranslatesCollectionOfTranslatables() throws Throwable {
        DummyTranslatable t1 = new DummyTranslatable();
        DummyTranslatable t2 = new DummyTranslatable();
        List<DummyTranslatable> list = new ArrayList<>(List.of(t1, t2));

        when(joinPoint.proceed()).thenReturn(list);

        Object result = aspect.translate(joinPoint);

        assertThat(result).isEqualTo(list);
        assertThat(t1.isTranslated()).isTrue();
        assertThat(t2.isTranslated()).isTrue();
    }

    @Test
    void testTranslateTranslatesNestedTranslatableProperties() throws Throwable {
        NestedEntity entity = new NestedEntity();
        entity.child = new DummyTranslatable();

        when(joinPoint.proceed()).thenReturn(entity);

        Object result = aspect.translate(joinPoint);

        assertThat(((NestedEntity) result).child.isTranslated()).isTrue();
    }

    @Test
    void testTranslateTranslatesMapKeysAndValues() throws Throwable {
        DummyTranslatable key = new DummyTranslatable();
        DummyTranslatable value = new DummyTranslatable();
        MapEntity entity = new MapEntity();
        entity.values.put(key, value);

        when(joinPoint.proceed()).thenReturn(entity);

        Object result = aspect.translate(joinPoint);

        MapEntity processed = (MapEntity) result;
        processed.values.forEach((k, v) -> {
            assertThat(((DummyTranslatable) k).isTranslated()).isTrue();
            assertThat(((DummyTranslatable) v).isTranslated()).isTrue();
        });
    }

    @Test
    void testTranslateIgnoresNullValues() throws Throwable {
        when(joinPoint.proceed()).thenReturn(null);

        Object result = aspect.translate(joinPoint);

        assertThat(result).isNull();
    }

    @Test
    void testTranslateUsesLocaleFromContextHolder() throws Throwable {
        LocaleContextHolder.setLocale(Locale.GERMAN);
        DummyTranslatable obj = spy(new DummyTranslatable());

        when(joinPoint.proceed()).thenReturn(obj);

        Object result = aspect.translate(joinPoint);

        assertThat(result).isEqualTo(obj);
        verify(obj).translate(Locale.GERMAN);
    }

}
