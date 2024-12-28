package com.arassec.artivact.core.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests a {@link RestrictedObject}.
 */
class RestrictedObjectTest {

    /**
     * Tests, that the object is forbidden for certain user's.
     */
    @Test
    void testIsForbidden() {
        RestrictionTest test = new RestrictionTest();

        // No restrictions:
        assertThat(test.isForbidden(Set.of(""))).isFalse();

        // Restrictions without user roles:
        test.getRestrictions().add("user");
        assertThat(test.isForbidden(null)).isTrue();
        assertThat(test.isForbidden(Set.of())).isTrue();

        // User has role:
        assertThat(test.isForbidden(Set.of("user"))).isFalse();
    }

    /**
     * A sample class, restricted to user's only.
     */
    private static class RestrictionTest implements RestrictedObject {

        /**
         * The restricted roles as strings.
         */
        private final Set<String> restrictions = new HashSet<>();

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<String> getRestrictions() {
            return restrictions;
        }
    }

}
