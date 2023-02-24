package com.arassec.artivact.creator.standalone.core.adapter.export;

import com.arassec.artivact.common.util.FileUtil;
import com.arassec.artivact.creator.standalone.core.model.*;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ArtivactVaultExportAdapter implements ExportAdapter {

    private final FileUtil fileUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getId() {
        return "adapter.implementation.export.artivact-vault";
    }

    @Override
    public void export(CreatorArtivact creatorArtivact, Path targetDir, ProgressMonitor progressMonitor) {
        Path exportDir = targetDir.resolve(creatorArtivact.getMainDir(false));

        fileUtil.deleteDir(exportDir);
        fileUtil.createDirIfRequired(exportDir);

        var imagesExported = exportImages(creatorArtivact, exportDir, creatorArtivact.getImageSets().stream()
                .map(ArtivactImageSet::getImages)
                .flatMap(Collection::stream)
                .filter(ArtivactImage::isExport)
                .toList());

        var modelsExported = exportModels(creatorArtivact, exportDir, creatorArtivact.getModels());

        var dataExported = exportData(creatorArtivact, exportDir);

        if (!imagesExported && !modelsExported && !dataExported) {
            creatorArtivact.deleteArtivactDir(targetDir);
        }
    }

    private boolean exportImages(CreatorArtivact creatorArtivact, Path exportDir, List<ArtivactImage> images) {
        if (images.isEmpty()) {
            return false;
        }

        Path imageExportDir = exportDir.resolve("images");
        fileUtil.createDirIfRequired(imageExportDir);

        for (var i = 0; i < images.size(); i++) {
            String targetFilename = String.format("%03d", i) + "." + determineFileEnding(images.get(i).getPath());
            try {
                Files.copy(creatorArtivact.getProjectRoot().resolve(images.get(i).getPath()), imageExportDir.resolve(targetFilename),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ArtivactCreatorException("Could not export image!", e);
            }
        }

        return true;
    }

    private boolean exportModels(CreatorArtivact creatorArtivact, Path exportDir, List<ArtivactModel> models) {
        var modelsExported = false;

        Path modelExportDir = exportDir.resolve("models");
        fileUtil.createDirIfRequired(modelExportDir);

        for (var i = 0; i < models.size(); i++) {
            for (var exportFile : models.get(i).getExportFiles()) {
                modelsExported = true;
                String targetFilename = String.format("%03d", i) + "." + determineFileEnding(exportFile);
                try {
                    Files.copy(creatorArtivact.getProjectRoot().resolve(creatorArtivact.getModelDir(true,
                                    models.get(i).getNumber())).resolve(exportFile), modelExportDir.resolve(targetFilename),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new ArtivactCreatorException("Could not export image!", e);
                }
            }
        }

        if (!modelsExported) {
            fileUtil.deleteDir(modelExportDir);
        }

        return modelsExported;
    }

    private boolean exportData(CreatorArtivact creatorArtivact, Path targetDir) {
        if (StringUtils.hasText(creatorArtivact.getNotes())) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("notes", creatorArtivact.getNotes());
                Files.writeString(targetDir.resolve("data.json").toAbsolutePath(), objectMapper.writeValueAsString(data));
            } catch (IOException e) {
                throw new ArtivactCreatorException("Could not create notes file during export!", e);
            }
            return true;
        }
        return false;
    }

    private String determineFileEnding(String fileName) {
        String[] parts = fileName.split("\\.");
        return parts[parts.length - 1];
    }
}
