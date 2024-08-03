package com.arassec.artivact.domain.creator.adapter.model.creator;

import java.nio.file.Path;

/**
 * Contains the result of a model creation process done by a {@link ModelCreatorAdapter}.
 *
 * @param resultDir The directory containing the resulting model files.
 * @param comment   The comment to use for adding the model to the item.
 */
public record ModelCreationResult(Path resultDir, String comment) {
}
