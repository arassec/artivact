package com.arassec.artivact.adapter.in.rest.controller;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.domain.exception.ArtivactException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public abstract class BaseImportController extends BaseController {


    protected abstract UseProjectDirsUseCase getUseProjectDirsUseCase();

    /**
     * Converts the provided multipart-file into a local, temporary file and returns its path.
     *
     * @param multipartFile The input file to save.
     * @return The created temporary file.
     */
    protected Path saveTempFile(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".tmp";

        Path tempFile = getUseProjectDirsUseCase().getTempDir().resolve("upload_" + suffix);
        try {
            if (tempFile.toFile().createNewFile()) {
                multipartFile.transferTo(tempFile.toFile());
                return tempFile;
            }
            throw new ArtivactException("Could not create empty, temporary file!");
        } catch (IOException e) {
            throw new ArtivactException("Could not create temporary file!", e);
        }
    }

}
