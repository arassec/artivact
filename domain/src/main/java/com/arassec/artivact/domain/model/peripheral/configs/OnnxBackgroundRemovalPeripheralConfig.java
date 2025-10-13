package com.arassec.artivact.domain.model.peripheral.configs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OnnxBackgroundRemovalPeripheralConfig extends PeripheralConfig {

    private String onnxModelFile;

    private String inputParameterName;

    private int imageWidth;

    private int imageHeight;

    private int numThreads;

}
