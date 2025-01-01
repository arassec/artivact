package com.arassec.artivact.domain.exchange.exporter;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.TranslatableObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Base class for exporters.
 */
public abstract class BaseExporter {

    /**
     * The object mapper.
     *
     * @return The {@link ObjectMapper} for object mapping.
     */
    public abstract ObjectMapper getObjectMapper();

    /**
     * Removes restrictions and a translated value from the supplied object.
     *
     * @param translatableRestrictedObject The object to clean up.
     */
    protected void cleanupTranslations(TranslatableObject translatableRestrictedObject) {
        Optional.ofNullable(translatableRestrictedObject).ifPresent(TranslatableObject::clear);
    }

    /**
     * Writes the supplied object as JSON file.
     *
     * @param targetPath The target path including the file name.
     * @param object     The object to write.
     */
    protected void writeJsonFile(Path targetPath, Object object) {
        try {
            getObjectMapper().writeValue(targetPath.toFile(), object);
        } catch (IOException e) {
            throw new ArtivactException("Could not write export file!", e);
        }
    }

}
