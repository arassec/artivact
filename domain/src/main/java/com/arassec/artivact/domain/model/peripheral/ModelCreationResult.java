package com.arassec.artivact.domain.model.peripheral;

import java.nio.file.Path;

/**
 * Contains the result of a model creation process.
 *
 * @param resultDir The directory containing the resulting model files.
 * @param comment   The comment to use for adding the model to the item.
 */
public record ModelCreationResult(Path resultDir, String comment) {
}
