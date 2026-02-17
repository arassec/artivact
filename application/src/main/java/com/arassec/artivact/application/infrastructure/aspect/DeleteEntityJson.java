package com.arassec.artivact.application.infrastructure.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to delete entity JSON file before execution.
 * The entity ID should be passed as a parameter to the method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeleteEntityJson {

    /**
     * The entity type, used to determine the subdirectory for JSON files.
     *
     * @return The entity type (e.g., "items", "pages", "menus").
     */
    String value();
}
