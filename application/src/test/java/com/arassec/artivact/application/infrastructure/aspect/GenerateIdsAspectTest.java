package com.arassec.artivact.application.infrastructure.aspect;

import com.arassec.artivact.domain.model.IdentifiedObject;
import com.arassec.artivact.domain.model.item.Item;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateIdsAspectTest {

    private final GenerateIdsAspect aspect = new GenerateIdsAspect();

    @Mock
    private JoinPoint joinPoint;

    static class TestEntity implements IdentifiedObject {
        NestedEntity nested;
        List<TestEntity> children = new ArrayList<>();
        String id;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }
    }

    static class NestedEntity implements IdentifiedObject {
        String id;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }
    }

    @Test
    void testGenerateIdForIdentifiedObjectWithoutId() {
        IdentifiedObject obj = new Item();
        obj.setId(null);

        when(joinPoint.getArgs()).thenReturn(new Object[]{obj});

        aspect.generateIds(joinPoint);

        assertThat(obj.getId()).isNotBlank();
        assertThat(obj.getId()).hasSizeGreaterThan(10);
    }

    @Test
    void testGenerateIdDoesNotOverwriteExistingId() {
        IdentifiedObject obj = new Item();
        obj.setId("already-set");

        when(joinPoint.getArgs()).thenReturn(new Object[]{obj});

        aspect.generateIds(joinPoint);

        assertThat(obj.getId()).isEqualTo("already-set");
    }

    @Test
    void testGeneratesIdsForCollectionElements() {
        IdentifiedObject obj1 = new Item();
        IdentifiedObject obj2 = new Item();
        Collection<IdentifiedObject> list = List.of(obj1, obj2);

        when(joinPoint.getArgs()).thenReturn(new Object[]{list});

        aspect.generateIds(joinPoint);

        assertThat(obj1.getId()).isNotBlank();
        assertThat(obj2.getId()).isNotBlank();
    }

    @Test
    void testGeneratesIdsForNestedObjects() {
        TestEntity root = new TestEntity();
        root.setId("root"); // root already has an id
        root.nested = new NestedEntity(); // no id
        root.children.add(new TestEntity()); // no id

        when(joinPoint.getArgs()).thenReturn(new Object[]{root});

        aspect.generateIds(joinPoint);

        assertThat(root.getId()).isEqualTo("root");
        assertThat(root.nested.getId()).isNotBlank();
        assertThat(root.children.getFirst().getId()).isNotBlank();
    }

}
