package com.arassec.artivact.domain.model.peripheral.configs;

import com.arassec.artivact.domain.model.IdentifiedObject;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeripheralConfig implements IdentifiedObject {

    private String id;

    private PeripheralImplementation peripheralImplementation;

    private String label;

    private boolean favourite;

}
