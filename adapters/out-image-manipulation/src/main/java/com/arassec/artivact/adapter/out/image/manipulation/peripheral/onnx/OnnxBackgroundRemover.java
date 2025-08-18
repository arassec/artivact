package com.arassec.artivact.adapter.out.image.manipulation.peripheral.onnx;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Removes the background from an image using an ONNX model.
 */
@Slf4j
@RequiredArgsConstructor
public class OnnxBackgroundRemover implements Runnable {

    /**
     * ONNX parameters for the background remover.
     */
    private final OnnxBackgroundRemoverParams params;

    /**
     * The result list containing paths to the processed image files without a background.
     */
    private final Collection<Path> processedFiles;

    /**
     * Path to the image file to remove the background from.
     */
    private final Path filePath;

    /**
     * Progress monitor to update.
     */
    private final ProgressMonitor progressMonitor;

    /**
     * Removes the background.
     */
    @Override
    public void run() {
        try {
            processedFiles.add(removeBackgroundFromImage(params, filePath));
        } catch (Exception e) {
            log.error("Could not remove background from image!", e);
        }
    }

    /**
     * Removes the background from the supplied input file and stores the resulting image in the provided output directory.
     *
     * @param params    The configuration parameters.
     * @param inputFile The original image with the background.
     * @return The path to the newly created image file.
     */
    private Path removeBackgroundFromImage(OnnxBackgroundRemoverParams params, Path inputFile) {
        try {
            BufferedImage img = ImageIO.read(inputFile.toFile());
            BufferedImage downscaledImage = downscaleImage(img, params.getOnnxInputImageSizeWidth(), params.getOnnxInputImageSizeHeight());

            float[][][][] inputData = loadImageAsCHW(downscaledImage, params.getOnnxInputImageSizeWidth(), params.getOnnxInputImageSizeHeight());
            OnnxTensor inputTensor = OnnxTensor.createTensor(params.getEnvironment(), inputData);

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put(params.getOnnxInputParameterName(), inputTensor);

            OrtSession.Result result = params.getSession().run(inputs);

            float[][][][] output = (float[][][][]) result.get(0).getValue();

            float[][] smallMask = output[0][0]; // [H][W] Mask from ONNX output

            BufferedImage maskImage = floatMaskToGrayImage(smallMask);
            BufferedImage resizedMaskImage = upscaleImage(maskImage, img.getWidth(), img.getHeight());
            BufferedImage finalImage = applyMask(img, resizedMaskImage);

            String[] fileNameParts = inputFile.getFileName().toString().split("\\.");
            String outputFilename = String.join(".", Arrays.copyOf(fileNameParts, fileNameParts.length - 1)) + ".png";
            Path outputFile = params.getTargetDir().resolve(outputFilename);

            ImageIO.write(finalImage, "png", outputFile.toFile());

            inputTensor.close();
            result.close();

            if (progressMonitor != null) {
                synchronized (progressMonitor) {
                    progressMonitor.updateProgress(progressMonitor.getCurrentAmount() + 1);
                }
            }

            return outputFile;
        } catch (IOException | OrtException e) {
            throw new ArtivactException("Could not process image file!", e);
        }
    }

    /**
     * Scales the image down to the size required by the ONNX model.
     *
     * @param img          The image to scale down.
     * @param targetWidth  The target width of the scaled image.
     * @param targetHeight The target height of the scaled image.
     * @return The scaled image.
     */
    private BufferedImage downscaleImage(BufferedImage img, int targetWidth, int targetHeight) {
        Image tmp = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_AREA_AVERAGING);
        BufferedImage downscaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = downscaledImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return downscaledImage;
    }

    /**
     * Scales the image up to the desired size.
     *
     * @param img          The image to scale up.
     * @param targetWidth  The target width of the scaled image.
     * @param targetHeight The target height of the scaled image.
     * @return The scaled image.
     */
    private BufferedImage upscaleImage(BufferedImage img, int targetWidth, int targetHeight) {
        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return output;
    }

    /**
     * Loads the image into the input array for the ONNX model.
     *
     * @param img       The image to load.
     * @param imgWidth  The width of the image.
     * @param imgHeight The height of the image.
     * @return The image as a float array.
     */
    private float[][][][] loadImageAsCHW(BufferedImage img, int imgWidth, int imgHeight) {

        if (img.getHeight() != imgHeight || img.getWidth() != imgWidth) {
            throw new ArtivactException("Image size does not match required size.");
        }

        // [1][3][H][W] Tensor preparation
        float[][][][] tensor = new float[1][3][imgHeight][imgWidth];

        for (int y = 0; y < imgHeight; y++) {
            for (int x = 0; x < imgWidth; x++) {
                Color color = new Color(img.getRGB(x, y));

                // normalize: 0.0 – 1.0
                tensor[0][0][y][x] = color.getRed() / 255.0f;   // R
                tensor[0][1][y][x] = color.getGreen() / 255.0f; // G
                tensor[0][2][y][x] = color.getBlue() / 255.0f;  // B
            }
        }

        return tensor;
    }

    /**
     * Converts the ONNX model's result into a grayscale image.
     *
     * @param mask The model's result.
     * @return A {@link BufferedImage} containing the grayscale image.
     */
    private BufferedImage floatMaskToGrayImage(float[][] mask) {
        int width = mask[0].length;
        int height = mask.length;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (int) (Math.clamp(mask[y][x], 0, 1) * 255);
                int rgb = (gray << 16) | (gray << 8) | gray;
                img.setRGB(x, y, rgb);
            }
        }
        return img;
    }

    /**
     * Applies the mask to the original image.
     *
     * @param original The original image.
     * @param mask     The image mask.
     * @return The masked image.
     */
    private BufferedImage applyMask(BufferedImage original, BufferedImage mask) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);
                int alpha = new Color(mask.getRGB(x, y)).getRed();
                if (alpha > 50) {
                    int rgba = (alpha << 24) | (rgb & 0x00FFFFFF);
                    result.setRGB(x, y, rgba);
                }
            }
        }

        return result;
    }

}
