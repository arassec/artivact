package com.arassec.artivact.domain.model.peripheral.configs;

import com.arassec.artivact.domain.model.IdentifiedObject;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for a peripheral device.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeripheralConfig implements IdentifiedObject {

    /**
     * The unique identifier.
     */
    private String id;

    /**
     * The peripheral implementation type.
     */
    private PeripheralImplementation peripheralImplementation;

    /**
     * The label for the peripheral.
     */
    private String label;

    /**
     * Indicates if this is a favourite configuration.
     */
    private boolean favourite;

}
