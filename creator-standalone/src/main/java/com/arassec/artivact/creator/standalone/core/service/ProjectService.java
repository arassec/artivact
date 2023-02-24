package com.arassec.artivact.creator.standalone.core.service;

import com.arassec.artivact.common.util.FileUtil;
import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.standalone.core.model.Project;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private static final String ARTIVACT_FILE_SUFFIX = ".artivact.json";

    private final FileUtil fileUtil;

    private final ObjectMapper objectMapper;

    public ProjectService(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Getter
    @Setter
    private Project activeProject;

    @Getter
    private CreatorArtivact activeCreatorArtivact;

    public boolean isProjectDir(Path projectRoot) {
        return Files.exists(projectRoot.resolve("Utils/checkerboard.png"));
    }

    public void initializeProjectDir(Path projectRoot) {
        updateProject(projectRoot);
    }

    public void updateProject(Path projectRoot) {
        fileUtil.createDirIfRequired(projectRoot.resolve("Data"));
        fileUtil.createDirIfRequired(projectRoot.resolve("Temp"));
        fileUtil.copyClasspathResource(Path.of("project-setup"), projectRoot);
        try {
            Path metashapeWorkflowFile = projectRoot.resolve("Utils/Metashape/artivact-metashape-workflow.xml");
            String metashapeWorkflow = Files.readString(metashapeWorkflowFile);
            metashapeWorkflow = metashapeWorkflow.replace("##EXPORT_PATH##",
                    projectRoot.resolve("Temp/metashape-export/metashape-export.obj").toAbsolutePath().toString());
            Files.writeString(metashapeWorkflowFile, metashapeWorkflow, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not update project!", e);
        }
    }

    public void initializeActiveArtivact(String artivactId) {
        activeCreatorArtivact = readArtivact(artivactId);
    }

    public void initializeActiveArtivact(CreatorArtivact creatorArtivact) {
        activeCreatorArtivact = creatorArtivact;
    }

    public CreatorArtivact createArtivact() {
        var artivact = new CreatorArtivact(UUID.randomUUID().toString());
        artivact.setProjectRoot(activeProject.getRootDir());
        artivact.setFileUtil(fileUtil);
        saveArtivact(artivact);
        return artivact;
    }

    public CreatorArtivact readArtivact(String artivactId) {
        var dummyArtivact = new CreatorArtivact(artivactId);
        dummyArtivact.setProjectRoot(activeProject.getRootDir());

        var artivactFile = Path.of(dummyArtivact.getMainDir(true).toString(),
                artivactId + ARTIVACT_FILE_SUFFIX);

        if (!Files.exists(artivactFile)) {
            throw new ArtivactCreatorException("Unkonwn artivact with ID: " + artivactFile);
        }

        try {
            var artivactJson = Files.readString(artivactFile);
            var artivact = objectMapper.readValue(artivactJson, CreatorArtivact.class);
            if (artivact.getImageSets() == null) {
                artivact.setImageSets(new LinkedList<>());
            }
            if (artivact.getModels() == null) {
                artivact.setModels(new LinkedList<>());
            }

            artivact.setProjectRoot(activeProject.getRootDir());
            artivact.setFileUtil(fileUtil);

            return artivact;
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not read artivact data set!", e);
        }
    }

    public void saveArtivact(CreatorArtivact creatorArtivact) {
        if (creatorArtivact == null || !StringUtils.hasText(creatorArtivact.getId())) {
            throw new ArtivactCreatorException("No ID on artivact to persist!");
        }

        String artivactId = creatorArtivact.getId();

        var artivactDir = creatorArtivact.getMainDir(true);
        var artivactFile = Path.of(artivactDir.toString(), artivactId + ARTIVACT_FILE_SUFFIX);

        fileUtil.createDirIfRequired(artivactDir);

        try {
            Files.writeString(artivactFile, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(creatorArtivact));
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not persist artivact data set!", e);
        }
    }

    public void deleteArtivact(CreatorArtivact creatorArtivact) {
        creatorArtivact.deleteArtivactDir(activeProject.getRootDir());
    }

    public List<String> getArtivactIds() {
        List<String> result = new LinkedList<>();
        findArtivactIdsRecursively(activeProject.getDataDir(), result);
        return result;
    }

    private void findArtivactIdsRecursively(Path root, List<String> target) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)) {
            directoryStream.forEach(path -> {
                if (path.getFileName().toString().endsWith(ARTIVACT_FILE_SUFFIX)) {
                    target.add(path.getFileName().toString().replace(ARTIVACT_FILE_SUFFIX, ""));
                } else if (Files.isDirectory(path)) {
                    findArtivactIdsRecursively(path, target);
                }
            });
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not read artivacts!", e);
        }
    }

}
