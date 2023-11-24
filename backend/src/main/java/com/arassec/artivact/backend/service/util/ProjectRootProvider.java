package com.arassec.artivact.backend.service.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Getter
public class ProjectRootProvider {

    private final Path projectRoot;

    public ProjectRootProvider(@Value("${artivact.vault.project.root:avdata}") String projectRootString) {
        this.projectRoot = Path.of(projectRootString);
    }

}
