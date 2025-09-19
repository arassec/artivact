package com.arassec.artivact.application.infrastructure.aspect;

import com.arassec.artivact.domain.model.RestrictedObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class RestrictResultAspectTest {

    private final RestrictResultAspect aspect = new RestrictResultAspect();

    @Mock
    private ProceedingJoinPoint joinPoint;

    static class TestRestricted implements RestrictedObject {
        private final boolean forbidden;

        TestRestricted(boolean forbidden) {
            this.forbidden = forbidden;
        }

        @Override
        public Set<String> getRestrictions() {
            return Set.of();
        }

        @Override
        public boolean isForbidden(Collection<String> roles) {
            return forbidden;
        }
    }

    static class NestedEntity {
        TestRestricted restricted;
    }

    enum DummyEnum {ONE, TWO}

    static class EntityWithEnum {
        @SuppressWarnings("unused")
        DummyEnum dummyEnum = DummyEnum.ONE;
    }

    @Test
    void testRestrictRestrictsForbiddenRestrictedObject() throws Throwable {
        TestRestricted restricted = new TestRestricted(true);
        when(joinPoint.proceed()).thenReturn(restricted);

        Object result = aspect.restrict(joinPoint);

        assertThat(result).isNull();
    }

    @Test
    void testRestrictAllowsPermittedRestrictedObject() throws Throwable {
        TestRestricted restricted = new TestRestricted(false);
        when(joinPoint.proceed()).thenReturn(restricted);

        Object result = aspect.restrict(joinPoint);

        assertThat(result).isEqualTo(restricted);
    }

    @Test
    void testRestrictFiltersForbiddenObjectsFromCollection() throws Throwable {
        TestRestricted allowed = new TestRestricted(false);
        TestRestricted forbidden = new TestRestricted(true);
        List<TestRestricted> list = new ArrayList<>(List.of(allowed, forbidden));

        when(joinPoint.proceed()).thenReturn(list);

        Object result = aspect.restrict(joinPoint);

        assertThat((Collection<TestRestricted>) result)
                .containsExactly(allowed)
                .doesNotContain(forbidden);
    }

    @Test
    void testRestrictRestrictsNestedRestrictedProperties() throws Throwable {
        NestedEntity entity = new NestedEntity();
        entity.restricted = new TestRestricted(true);

        when(joinPoint.proceed()).thenReturn(entity);

        Object result = aspect.restrict(joinPoint);

        assertThat(((NestedEntity) result).restricted).isNull();
    }

    @Test
    void testRestrictReturnsNullForEnums() throws Throwable {
        EntityWithEnum entity = new EntityWithEnum();
        when(joinPoint.proceed()).thenReturn(entity);

        Object result = aspect.restrict(joinPoint);

        assertThat(result).isNull();
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testRestrictGetRolesExtractsAuthorities() throws Throwable {
        GrantedAuthority auth = () -> "ROLE_ADMIN";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getAuthorities()).thenReturn((Collection) List.of(auth));

        var authentication = mock(org.springframework.security.core.Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        TestRestricted restricted = new TestRestricted(false);
        when(joinPoint.proceed()).thenReturn(restricted);

        Object result = aspect.restrict(joinPoint);

        assertThat(result).isEqualTo(restricted);
    }

}
