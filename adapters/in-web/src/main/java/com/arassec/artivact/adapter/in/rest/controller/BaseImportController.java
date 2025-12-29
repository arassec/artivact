package com.arassec.artivact.adapter.in.rest.controller;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * REST controller for base import.
 */
public abstract class BaseImportController extends BaseController {


    /**
     * Returns the use project dirs use case.
     */
    protected abstract UseProjectDirsUseCase getUseProjectDirsUseCase();

    /**
     * Returns the file repository.
     */
    protected abstract FileRepository getFileRepository();

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

        Path tempFile = getUseProjectDirsUseCase().getTempDir().resolve("upload_"
                + Optional.ofNullable(originalFilename).orElse("").replace(suffix, "") + suffix);
        try {
            getFileRepository().copy(multipartFile.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        } catch (IOException e) {
            throw new ArtivactException("Could not create temporary file!", e);
        }
    }

}
