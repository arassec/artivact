package com.arassec.artivact.adapter.out.image.manipulation;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.arassec.artivact.adapter.out.image.manipulation.onnx.OnnxBackgroundRemover;
import com.arassec.artivact.adapter.out.image.manipulation.onnx.OnnxBackgroundRemoverParams;
import com.arassec.artivact.domain.model.peripheral.PeripheralAdapterInitParams;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
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
public class OnnxBackgroundRemoverTest {

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

        OnnxBackgroundRemoverParams params = new OnnxBackgroundRemoverParams(
                "silueta.onnx#input.1#320#320#5",
                PeripheralAdapterInitParams.builder()
                        .workDir(Path.of("target"))
                        .build()
        );

        OrtEnvironment environment = OrtEnvironment.getEnvironment();

        OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
        sessionOptions.setInterOpNumThreads(4);
        sessionOptions.addCPU(true);

        OrtSession session = environment.createSession(Path.of("../../application/src/main/resources/project-setup/utils/onnx/silueta.onnx").toString(), sessionOptions);

        params.setEnvironment(environment);
        params.setSession(session);

        OnnxBackgroundRemover backgroundRemover = new OnnxBackgroundRemover(params, result, filePath, progressMonitor);

        backgroundRemover.run();

        Path resultImage = Path.of("target/background-test.png");

        assertThat(resultImage).exists();
        assertThat(Files.size(resultImage)).isGreaterThan(0);
    }

}
