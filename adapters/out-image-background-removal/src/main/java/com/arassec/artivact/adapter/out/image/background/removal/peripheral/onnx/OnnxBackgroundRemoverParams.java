package com.arassec.artivact.adapter.out.image.background.removal.peripheral.onnx;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.arassec.artivact.domain.model.peripheral.configs.OnnxBackgroundRemovalPeripheralConfig;
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
     * @param config    Peripheral configuration parameters.
     * @param targetDir The target dir for the modified images.
     */
    public OnnxBackgroundRemoverParams(OnnxBackgroundRemovalPeripheralConfig config, Path targetDir) {

        onnxModelFileName = config.getOnnxModelFile();
        onnxInputParameterName = config.getInputParameterName();
        onnxInputImageSizeWidth = config.getImageWidth();
        onnxInputImageSizeHeight = config.getImageHeight();

        numThreads = config.getNumThreads();

        this.targetDir = targetDir;
    }

}
