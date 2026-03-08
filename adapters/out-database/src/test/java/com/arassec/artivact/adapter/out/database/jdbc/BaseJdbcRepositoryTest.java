package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.domain.exception.ArtivactException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the {@link BaseJdbcRepository}.
 */
class BaseJdbcRepositoryTest {

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();

    /**
     * Concrete implementation for testing.
     */
    private class TestableBaseJdbcRepository extends BaseJdbcRepository {
        @Override
        protected JsonMapper getJsonMapper() {
            return jsonMapper;
        }

        public String testToJson(Object object) {
            return toJson(object);
        }

        public <T> T testFromJson(String json, Class<T> clazz) {
            return fromJson(json, clazz);
        }
    }

    /**
     * Simple test class for serialization.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestObject {
        private String name;
        private int value;
    }

    /**
     * Test class without no-args constructor to test exception handling.
     */
    public record NoDefaultConstructorObject(String name) {
    }

    private final TestableBaseJdbcRepository repository = new TestableBaseJdbcRepository();

    /**
     * Tests serializing an object to JSON.
     */
    @Test
    void testToJson() {
        TestObject testObject = new TestObject("test", 42);
        String json = repository.testToJson(testObject);

        assertThat(json).contains("\"name\" : \"test\"").contains("\"value\" : 42");
    }

    /**
     * Tests serializing null returns null.
     */
    @Test
    void testToJsonWithNull() {
        String json = repository.testToJson(null);
        assertThat(json).isNull();
    }

    /**
     * Tests deserializing JSON to an object.
     */
    @Test
    void testFromJson() {
        String json = "{\"name\":\"test\",\"value\":42}";
        TestObject result = repository.testFromJson(json, TestObject.class);

        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getValue()).isEqualTo(42);
    }

    /**
     * Tests deserializing null, empty or whitespace JSON returns default instance.
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void testFromJsonWithUnparsableContent(String json) {
        TestObject result = repository.testFromJson(json, TestObject.class);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isNull();
        assertThat(result.getValue()).isZero();
    }

    /**
     * Tests that missing no-args constructor throws ArtivactException.
     */
    @Test
    void testFromJsonWithNoDefaultConstructorThrowsException() {
        assertThatThrownBy(() -> repository.testFromJson("", NoDefaultConstructorObject.class))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not create default instance of object!");
    }

}
