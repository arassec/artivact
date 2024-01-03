package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.service.util.FileModification;
import com.arassec.artivact.backend.service.util.FileUtil;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRootProvider projectRootProvider;

    private final FileUtil fileUtil;

    @PostConstruct
    public void initializeProjectDir() {
        fileUtil.updateProjectDirectory(projectRootProvider.getProjectRoot(), List.of(
                new FileModification("utils/Metashape/artivact-metashape-workflow.xml", "##EXPORT_PATH##",
                        projectRootProvider.getProjectRoot().resolve("temp/metashape-export/metashape-export.obj").toAbsolutePath().toString())
        ));
    }

}
