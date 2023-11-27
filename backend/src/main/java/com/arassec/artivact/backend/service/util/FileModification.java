package com.arassec.artivact.backend.service.util;

/**
 * Defines a file and its modification during project setup.
 *
 * @param file        The file in the project directory to modify during project setup.
 * @param placeholder A placeholder String inside the file, which will be replaced.
 * @param replacement The replacement for the placeholder.
 */
public record FileModification(String file, String placeholder, String replacement) {
}
