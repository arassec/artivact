package com.arassec.artivact.adapter.out.image.background.removal.peripheral.onnx;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.configs.OnnxBackgroundRemovalPeripheralConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link OnnxBackgroundRemover}.
 */
class OnnxBackgroundRemoverTest {

    /**
     * Tests removing the background from an image.
     *
     * @throws OrtException In case of ONNX errors.
     * @throws IOException  In case of file IO errors.
     */
    @Test
    void testRemoveBackgroundFromImage() throws OrtException, IOException {

        List<Path> result = new ArrayList<>();
        Path filePath = Path.of("src/test/resources/background-test.jpg");
        ProgressMonitor progressMonitor = new ProgressMonitor("OnnxBackgroundRemoverTest", "test");

        OnnxBackgroundRemoverParams params = getOnnxBackgroundRemoverParams();

        OnnxBackgroundRemover backgroundRemover = new OnnxBackgroundRemover(params, result, filePath, progressMonitor);

        backgroundRemover.run();

        assertThat(result).hasSize(1);
        Path resultImage = result.getFirst();

        assertThat(resultImage).exists();
        assertThat(Files.size(resultImage)).isGreaterThan(0);
    }

    private OnnxBackgroundRemoverParams getOnnxBackgroundRemoverParams() throws OrtException {
        OnnxBackgroundRemoverParams params = new OnnxBackgroundRemoverParams(
                new OnnxBackgroundRemovalPeripheralConfig("silueta.onnx", "input.1", 320, 320, 5),
                Path.of("target")
        );

        OrtEnvironment environment = OrtEnvironment.getEnvironment();

        OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
        sessionOptions.setInterOpNumThreads(4);
        sessionOptions.addCPU(true);

        OrtSession session = environment.createSession(Path.of("../../application/src/main/resources/project-setup/utils/onnx/silueta.onnx").toString(), sessionOptions);

        params.setEnvironment(environment);
        params.setSession(session);

        return params;
    }

}
