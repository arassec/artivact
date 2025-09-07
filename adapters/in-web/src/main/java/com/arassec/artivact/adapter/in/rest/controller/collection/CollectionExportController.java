package com.arassec.artivact.adapter.in.rest.controller.collection;

import com.arassec.artivact.adapter.in.rest.controller.BaseImportController;
import com.arassec.artivact.application.port.in.collection.*;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.misc.ExchangeDefinitions;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * REST-Controller for collection export management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/collection/export")
public class CollectionExportController extends BaseImportController {

    /**
     * Use case for project directory handling.
     */
    @Getter
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Repository for file access.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * Use case to import collections.
     */
    private final ImportCollectionUseCase importCollectionUseCase;

    /**
     * Use case to load a collection export.
     */
    private final LoadCollectionExportUseCase loadCollectionExportUseCase;

    /**
     * Use case to save collection exports.
     */
    private final SaveCollectionExportUseCase saveCollectionExportUseCase;

    /**
     * Use case to delete collection exports.
     */
    private final DeleteCollectionExportUseCase deleteCollectionExportUseCase;

    /**
     * Use case to build a collection export file.
     */
    private final BuildCollectionExportFileUseCase buildCollectionExportFileUseCase;

    /**
     * Use case to read infos from a collection export file.
     */
    private final ReadCollectionExportFileUseCase readCollectionExportFileUseCase;

    /**
     * Use case to save a cover picture of a collection export.
     */
    private final SaveCollectionExportCoverPictureUseCase saveCollectionExportCoverPictureUseCase;

    /**
     * Use case to load a collection export's cover picture.
     */
    private final LoadCollectionExportCoverPictureUseCase loadCollectionExportCoverPictureUseCase;

    /**
     * Use case do delete a collection export's cover picture.
     */
    private final DeleteCollectionExportCoverPictureUseCase deleteCollectionExportCoverPictureUseCase;

    /**
     * Use case to create collection export infos.
     */
    private final CreateCollectionExportInfosUseCase createCollectionExportInfosUseCase;

    /**
     * Returns the available collection exports.
     *
     * @return List of {@link CollectionExport}s.
     */
    @GetMapping
    public List<CollectionExport> loadCollectionExports() {
        return loadCollectionExportUseCase.loadAll();
    }

    /**
     * Saves a new collection export with the given parameters.
     *
     * @param collectionExport The collection export to save.
     * @return List of {@link CollectionExport}s.
     */
    @PostMapping
    public List<CollectionExport> save(@RequestBody CollectionExport collectionExport) {
        saveCollectionExportUseCase.save(collectionExport);
        return loadCollectionExports();
    }

    /**
     * Saves the sort order of all supplied collection exports.
     *
     * @param collectionExports The collection exports to save.
     * @return List of {@link CollectionExport}s.
     */
    @PostMapping("/sort")
    public List<CollectionExport> saveSortOrder(@RequestBody List<CollectionExport> collectionExports) {
        saveCollectionExportUseCase.saveSortOrder(collectionExports);
        return loadCollectionExports();
    }

    /**
     * Deletes an existing collection export.
     *
     * @param id The ID of the collection export to delete.
     */
    @DeleteMapping("/{id}")
    public List<CollectionExport> delete(@PathVariable String id) {
        if (StringUtils.hasText(id)) {
            deleteCollectionExportUseCase.delete(id);
        }
        return loadCollectionExports();
    }

    /**
     * Creates a new collection export with the given parameters.
     *
     * @param id The ID of the collection export to build.
     */
    @PostMapping("/{id}/build")
    public void build(@PathVariable String id) {
        buildCollectionExportFileUseCase.buildExportFile(id);
    }

    /**
     * Returns the export file for the collection export with the given ID.
     *
     * @param id             The ID of the collection providing the export file.
     * @param authentication The user's authentication.
     * @param response       The HTTP stream to write the exported JSON file to.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/{id}/file")
    public ResponseEntity<StreamingResponseBody> downloadExportFile(@PathVariable String id, Authentication authentication, HttpServletResponse response) {
        List<String> availableRoles;
        if (authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            availableRoles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        } else {
            availableRoles = List.of();
        }

        CollectionExport collectionExport = loadCollectionExportUseCase.load(id);

        if (!collectionExport.getRestrictions().isEmpty()) {
            Optional<String> matchingRoleOptional = collectionExport.getRestrictions().stream()
                    .filter(availableRoles::contains)
                    .findAny();
            if (matchingRoleOptional.isEmpty()) {
                throw new ArtivactException("Operation not allowed!");
            }
        }

        if (!collectionExport.isFilePresent()) {
            return ResponseEntity.notFound().build();
        }

        StreamingResponseBody streamResponseBody = out -> {
            StreamUtils.copy(readCollectionExportFileUseCase.readExportFile(id), out);
            response.setContentLength(Math.toIntExact(collectionExport.getFileSize()));
        };

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX
                + id + ExchangeDefinitions.COLLECTION_EXCHANGE_SUFFIX + ExchangeDefinitions.ZIP_FILE_SUFFIX);
        response.addHeader(HttpHeaders.PRAGMA, NO_CACHE);
        response.addHeader(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);

        return ResponseEntity.ok(streamResponseBody);
    }

    /**
     * Saves the cover picture of a collection export.
     *
     * @param id   The collection export's ID.
     * @param file The uploaded cover-image.
     */
    @PostMapping("/{id}/cover-picture")
    public void saveCoverPicture(@PathVariable String id,
                                 @RequestPart(value = "file") final MultipartFile file) {
        synchronized (this) {
            try {
                saveCollectionExportCoverPictureUseCase.saveCoverPicture(id, file.getOriginalFilename(), file.getInputStream());
            } catch (IOException e) {
                throw new ArtivactException("Could not save uploaded cover picture!", e);
            }
        }
    }

    /**
     * Returns the cover picture of a collection export.
     *
     * @param id     The collection export's ID.
     * @param random Prevent caching.
     * @return The uploaded cover-image.
     */
    @SuppressWarnings("unused") // "random" is used, just not in code...
    @GetMapping("/{id}/cover-picture/{random}")
    public HttpEntity<byte[]> loadCoverPicture(@PathVariable String id, @PathVariable String random) {
        CollectionExport collectionExport = loadCollectionExportUseCase.load(id);

        String filename = Instant.now().toEpochMilli() + "." + collectionExport.getCoverPictureExtension();

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(URLConnection.guessContentTypeFromName(filename)));
        headers.set(HttpHeaders.PRAGMA, NO_CACHE);
        headers.set(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());

        byte[] image = loadCollectionExportCoverPictureUseCase.loadCoverPicture(id);

        return new HttpEntity<>(image, headers);
    }

    /**
     * Deletes the cover picture of a collection export.
     *
     * @param id The collection export's ID.
     */
    @DeleteMapping("/{id}/cover-picture")
    public List<CollectionExport> deleteCoverPicture(@PathVariable String id) {
        deleteCollectionExportCoverPictureUseCase.deleteCoverPicture(id);
        return loadCollectionExports();
    }

    /**
     * Returns infos about available collection exports.
     *
     * @return ZIP file containing the information about available content exports.
     */
    @GetMapping("/info")
    public ResponseEntity<StreamingResponseBody> loadCollectionExportInfos(HttpServletResponse response) {
        StreamingResponseBody streamResponseBody = outputStream -> createCollectionExportInfosUseCase.createCollectionExportInfos(outputStream,
                loadCollectionExportUseCase.loadAllRestricted());

        response.setContentType(TYPE_ZIP);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX + ExchangeDefinitions.COLLECTION_EXPORT_OVERVIEWS_ZIP_FILE);

        return ResponseEntity.ok(streamResponseBody);
    }

    /**
     * Imports a collection export and its content into the application.
     *
     * @param file The collection export to import.
     */
    @PostMapping("/import")
    public void importCollection(@RequestPart(value = "file") final MultipartFile file) {
        Path tempFile = saveTempFile(file);
        importCollectionUseCase.importCollection(tempFile);
    }

    /**
     * Imports a collection export for distribution into the application.
     *
     * @param file The collection export to import.
     */
    @PostMapping("/import/for-distribution")
    public void importCollectionForDistribution(@RequestPart(value = "file") final MultipartFile file) {
        Path tempFile = saveTempFile(file);
        importCollectionUseCase.importCollectionForDistribution(tempFile);
    }

}
