package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.domain.exchange.ExchangeProcessor;
import com.arassec.artivact.domain.service.CollectionExportService;
import com.arassec.artivact.web.model.OperationProgress;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.net.URLConnection;
import java.time.Instant;
import java.util.List;

/**
 * REST-Controller for collection export management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/collection/export")
public class CollectionExportController extends BaseController {

    /**
     * Service for collection exports.
     */
    private final CollectionExportService collectionExportService;

    /**
     * Returns the available collection exports.
     *
     * @return List of {@link CollectionExport}s.
     */
    @GetMapping("/public")
    public List<CollectionExport> loadPublicCollectionExports() {
        return collectionExportService.loadAllRestricted();
    }

    /**
     * Returns the available collection exports.
     *
     * @return List of {@link CollectionExport}s.
     */
    @GetMapping
    public List<CollectionExport> loadCollectionExports() {
        return collectionExportService.loadAll();
    }

    /**
     * Creates a new collection export with the given parameters.
     *
     * @param collectionExport The collection export to save.
     * @return List of {@link CollectionExport}s.
     */
    @PostMapping
    public List<CollectionExport> save(@RequestBody CollectionExport collectionExport) {
        collectionExportService.save(collectionExport);
        return loadCollectionExports();
    }

    /**
     * Creates a new collection export with the given parameters.
     *
     * @param collectionExports The collection exports to save.
     * @return List of {@link CollectionExport}s.
     */
    @PostMapping("/sort")
    public List<CollectionExport> saveSortOrder(@RequestBody List<CollectionExport> collectionExports) {
        collectionExportService.saveSortOrder(collectionExports);
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
            collectionExportService.delete(id);
        }
        return loadCollectionExports();
    }

    /**
     * Creates a new collection export with the given parameters.
     *
     * @param id The ID of the collection export to build.
     * @return The {@link OperationProgress} to track the progress of export creation.
     */
    @PostMapping("/{id}/build")
    public ResponseEntity<OperationProgress> build(@PathVariable String id) {
        collectionExportService.build(id);
        return getProgress();
    }

    /**
     * Returns the export file for the collection export with the given ID.
     *
     * @param id       The ID of the collection providing the export file.
     * @param response The HTTP stream to write the exported JSON file to.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/{id}/export-file")
    public ResponseEntity<StreamingResponseBody> downloadExportFile(@PathVariable String id, HttpServletResponse response) {

        CollectionExport collectionExport = collectionExportService.load(id);

        if (!collectionExport.isFilePresent()) {
            return ResponseEntity.notFound().build();
        }

        StreamingResponseBody streamResponseBody = out -> {
            StreamUtils.copy(collectionExportService.readExportFile(id), out);
            response.setContentLength(Math.toIntExact(collectionExport.getFileSize()));
        };

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX
                + id + ExchangeProcessor.COLLECTION_EXCHANGE_SUFFIX + ExchangeProcessor.ZIP_FILE_SUFFIX);
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
                collectionExportService.saveCoverPicture(id, file.getOriginalFilename(), file.getInputStream());
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
     * @return file   The uploaded cover-image.
     */
    @SuppressWarnings("unused") // "random" is used, just not in code...
    @GetMapping("/{id}/cover-picture/{random}")
    public HttpEntity<byte[]> loadCoverPicture(@PathVariable String id, @PathVariable String random) {
        CollectionExport collectionExport = collectionExportService.load(id);

        String filename = Instant.now().toEpochMilli() + "." + collectionExport.getCoverPictureExtension();

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(URLConnection.guessContentTypeFromName(filename)));
        headers.set(HttpHeaders.PRAGMA, NO_CACHE);
        headers.set(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());

        byte[] image = collectionExportService.loadCoverPicture(id);

        return new HttpEntity<>(image, headers);
    }

    /**
     * Deletes the cover picture of a collection export.
     *
     * @param id The collection export's ID.
     */
    @DeleteMapping("/{id}/cover-picture")
    public List<CollectionExport> deleteCoverPicture(@PathVariable String id) {
        collectionExportService.deleteCoverPicture(id);
        return loadCollectionExports();
    }

    /**
     * Imports a collection export and its content into the application.
     *
     * @param file The collection export to import.
     * @return The import progress.
     */
    @PostMapping("/import")
    public ResponseEntity<OperationProgress> importCollection(@RequestPart(value = "file") final MultipartFile file) {
        collectionExportService.importCollection(file);
        return getProgress();
    }

    /**
     * Imports a collection export for distribution into the application.
     *
     * @param file The collection export to import.
     * @return The import progress.
     */
    @PostMapping("/import/for-distribution")
    public ResponseEntity<OperationProgress> importCollectionForDistribution(@RequestPart(value = "file") final MultipartFile file) {
        collectionExportService.importCollectionForDistribution(file);
        return getProgress();
    }

    /**
     * Returns the progress of a previously started long-running operation.
     *
     * @return The progress.
     */
    @GetMapping("/progress")
    public ResponseEntity<OperationProgress> getProgress() {
        return convert(collectionExportService.getProgressMonitor());
    }

}
