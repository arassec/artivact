package com.arassec.artivact.backend.service.aop;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.RestrictedObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements the {@link RestrictResult} aspect.
 */
@Aspect
@Component
public class RestrictResultAspect {

    /**
     * Only classes from this package (and sub-packages) are processed.
     */
    private static final String ARTIVACT_PACKAGE_PREFIX = "com.arassec.artivact";

    /**
     * Executes the method and processes its result.
     *
     * @param joinPoint The aspectj {@link ProceedingJoinPoint}.
     * @return The methods return value, cleaned by non-allowed elements.
     * @throws Throwable In case of errors.
     */
    @Around("@annotation(com.arassec.artivact.backend.service.aop.RestrictResult)")
    public Object restrict(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        Set<String> roles = getRoles(SecurityContextHolder.getContext().getAuthentication());
        return restrictIfRequired(result, roles);
    }

    /**
     * Restricts the object recursively if it is a {@link RestrictedObject} and the user doesn't have the necessary roles.
     *
     * @param object The object to restrict.
     * @param roles  The current user's roles.
     * @return The restricted object.
     */
    private Object restrictIfRequired(Object object, Set<String> roles) {
        if (object instanceof RestrictedObject restrictedObject && (restrictedObject.isForbidden(roles))) {
            return null;
        } else if (object instanceof Collection<?> collectionToFilter) {
            return filterCollection(collectionToFilter, roles);
        }
        return restrictPropertiesIfRequired(object, roles);
    }

    /**
     * Restricts an object's properties if required.
     *
     * @param object The object to process.
     * @param roles  The current user's roles.
     * @return The object with restricted properties.
     */
    @SuppressWarnings("java:S3011") // declaredField.setAccessible(true) is intentional here!
    private Object restrictPropertiesIfRequired(Object object, Set<String> roles) {
        if (object == null) {
            return null;
        }
        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                if (field.getType().getName().startsWith(ARTIVACT_PACKAGE_PREFIX)) {
                    field.setAccessible(true);
                    Object declaredFieldValue = field.get(object);
                    if (declaredFieldValue instanceof RestrictedObject restrictedObject && (restrictedObject.isForbidden(roles))) {
                        field.set(object, null);
                    } else {
                        field.set(object, restrictIfRequired(declaredFieldValue, roles));
                    }
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Collection<?> collection = (Collection<?>) field.get(object);
                    field.set(object, filterCollection(collection, roles));
                }
            } catch (IllegalAccessException e) {
                throw new ArtivactException("Could not restrict method result!", e);
            }
        }
        return object;
    }

    /**
     * Filters restricted objects from a collection.
     *
     * @param toBeFiltered The collection.
     * @param roles        The current user's roles.
     * @return The filtered collection.
     */
    private Collection<?> filterCollection(Collection<?> toBeFiltered, Set<String> roles) {
        // Filter the collection entries themselves:
        List<?> toBeRemoved = toBeFiltered.stream()
                .filter(item -> (item instanceof RestrictedObject restrictedItem && restrictedItem.isForbidden(roles)))
                .toList();
        if (!toBeRemoved.isEmpty()) {
            toBeRemoved.forEach(toBeFiltered::remove);
        }
        // Filter properties of collection entries:
        toBeFiltered.forEach(entry -> restrictPropertiesIfRequired(entry, roles));
        return toBeFiltered;
    }

    /**
     * Returns the roles of the current user.
     *
     * @param authentication Spring-Security's {@link Authentication}.
     * @return The user's roles.
     */
    private Set<String> getRoles(Authentication authentication) {
        if (authentication == null) {
            return Set.of();
        }
        if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return Set.of();
        }
        if (userDetails.getAuthorities() == null) {
            return Set.of();
        }
        return userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

}
