package com.arassec.artivact.domain.exchange.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * Context for an import.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportContext {

    /**
     * The base directory where data to import lies in.
     */
    private Path importDir;

}
