package com.arassec.artivact.backend.service.model.item.asset;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

/**
 * An asset associated to an item.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class Asset {

    /**
     * The asset's number.
     */
    protected int number;

    /**
     * Path to the actual file.
     */
    protected String path;

}
