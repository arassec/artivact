package com.arassec.artivact.adapter.out.image.manipulation.onnx;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.adapter.PeripheralAdapterInitParams;
import lombok.Data;

import java.nio.file.Path;

/**
 * Parameters for background removal with ONNX models.
 */
@Data
public class OnnxBackgroundRemoverParams {

    /**
     * The ONNX runtime environment.
     */
    protected OrtEnvironment environment;

    /**
     * The ONNX runtime session.
     */
    protected OrtSession session;

    /**
     * The name of the ONNX model file.
     */
    private String onnxModelFileName;

    /**
     * The name of the input parameter of the model.
     */
    private String onnxInputParameterName;

    /**
     * Width of the input image.
     */
    private int onnxInputImageSizeWidth;

    /**
     * Height of the input image.
     */
    private int onnxInputImageSizeHeight;

    /**
     * Number of threads to use for background removal.
     */
    private int numThreads;

    /**
     * Target directory for processed images.
     */
    private Path targetDir;

    /**
     * Creates new parameters for background removal.
     *
     * @param adapterConfiguration Adapter configuration as string.
     * @param initParams           Init parameters.
     */
    public OnnxBackgroundRemoverParams(String adapterConfiguration, PeripheralAdapterInitParams initParams) {
        String[] configuration = adapterConfiguration.split("#");
        if (configuration.length != 5) {
            throw new ArtivactException("Invalid configuration for background removal adapter!");
        }

        onnxModelFileName = configuration[0];
        onnxInputParameterName = configuration[1];
        onnxInputImageSizeWidth = Integer.parseInt(configuration[2]);
        onnxInputImageSizeHeight = Integer.parseInt(configuration[3]);

        numThreads = Integer.parseInt(configuration[4]);

        targetDir = initParams.getWorkDir();
    }

}
