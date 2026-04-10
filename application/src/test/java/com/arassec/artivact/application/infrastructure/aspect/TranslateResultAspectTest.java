package com.arassec.artivact.application.infrastructure.aspect;

import com.arassec.artivact.application.port.out.repository.ConfigurationRepository;
import com.arassec.artivact.domain.model.TranslatableObject;
import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;
import com.arassec.artivact.domain.model.configuration.ConfigurationType;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslateResultAspectTest {

    @InjectMocks
    private TranslateResultAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Getter
    static class DummyTranslatable implements TranslatableObject {
        boolean translated = false;
        String lastDefaultLocale = null;

        @Override
        public void translate(String locale) {
            translated = true;
        }

        @Override
        public void translate(String locale, String defaultLocale) {
            translated = true;
            lastDefaultLocale = defaultLocale;
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
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.empty());

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
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.empty());

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
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.empty());

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
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.empty());

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
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.empty());

        Object result = aspect.translate(joinPoint);

        assertThat(result).isNull();
    }

    @Test
    void testTranslateUsesLocaleFromContextHolder() throws Throwable {
        LocaleContextHolder.setLocale(Locale.GERMAN);
        DummyTranslatable obj = new DummyTranslatable();

        when(joinPoint.proceed()).thenReturn(obj);
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.empty());

        Object result = aspect.translate(joinPoint);

        assertThat(result).isEqualTo(obj);
        assertThat(obj.isTranslated()).isTrue();
        assertThat(obj.getLastDefaultLocale()).isEqualTo("en");
    }

    @Test
    void testTranslateUsesConfiguredDefaultLocale() throws Throwable {
        LocaleContextHolder.setLocale(Locale.JAPANESE);
        DummyTranslatable obj = new DummyTranslatable();

        AppearanceConfiguration config = new AppearanceConfiguration();
        config.setDefaultLocale("de");

        when(joinPoint.proceed()).thenReturn(obj);
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.of(config));

        Object result = aspect.translate(joinPoint);

        assertThat(result).isEqualTo(obj);
        assertThat(obj.isTranslated()).isTrue();
        assertThat(obj.getLastDefaultLocale()).isEqualTo("de");
    }

}
