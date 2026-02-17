package com.arassec.artivact.application.infrastructure.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to persist its entity parameter(s) as JSON files after successful execution.
 * The entity type directory (items, pages, menus) must be specified.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PersistEntityAsJson {

    /**
     * The subdirectory name under the project root for this entity type (e.g. "items", "pages", "menus").
     *
     * @return The entity type directory.
     */
    String entityDir();

    /**
     * The type of the entity to persist from the method's parameters.
     *
     * @return The entity class.
     */
    Class<?> entityType();

    /**
     * Whether this is a delete operation. If true, the JSON file will be removed instead of written.
     *
     * @return {@code true} if the entity JSON file should be deleted.
     */
    boolean delete() default false;

}
