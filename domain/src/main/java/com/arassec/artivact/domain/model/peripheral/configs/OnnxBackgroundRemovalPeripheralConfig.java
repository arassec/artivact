package com.arassec.artivact.domain.model.peripheral.configs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuration for an ONNX background removal peripheral.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OnnxBackgroundRemovalPeripheralConfig extends PeripheralConfig {

    /**
     * The ONNX model file path.
     */
    private String onnxModelFile;

    /**
     * The input parameter name for the ONNX model.
     */
    private String inputParameterName;

    /**
     * The image width for processing.
     */
    private int imageWidth;

    /**
     * The image height for processing.
     */
    private int imageHeight;

    /**
     * The number of threads to use for processing.
     */
    private int numThreads;

}
