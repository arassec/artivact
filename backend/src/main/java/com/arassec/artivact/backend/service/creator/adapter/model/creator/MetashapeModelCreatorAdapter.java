package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.util.CmdUtil;
import com.arassec.artivact.backend.service.util.FileUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Adapter implementation that uses "Metashape" for model creation.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class MetashapeModelCreatorAdapter extends BaseModelCreatorAdapter {

    /**
     * The commandline util.
     */
    private final CmdUtil cmdUtil;

    /**
     * The file util.
     */
    private final FileUtil fileUtil;

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images) {
        Path tempDir = initParams.getTempDir();

        Path resultDir = tempDir.resolve(EXPORT_DIR).toAbsolutePath();

        fileUtil.emptyDir(tempDir);

        copyImages(images, tempDir, progressMonitor);

        fileUtil.openDirInOs(tempDir);

        progressMonitor.updateLabelKey("createModelStart");

        cmdUtil.execute(new CommandLine(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation())));

        return new ModelCreationResult(resultDir, "metashape");
    }

}
