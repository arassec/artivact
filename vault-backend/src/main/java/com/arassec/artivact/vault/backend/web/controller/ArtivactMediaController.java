package com.arassec.artivact.vault.backend.web.controller;

import com.arassec.artivact.vault.backend.core.ArtivactVaultException;
import com.arassec.artivact.vault.backend.service.MediaService;
import com.arassec.artivact.vault.backend.service.model.ImageSize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/artivact/media")
public class ArtivactMediaController {

    private final MediaService mediaService;

    @GetMapping(value = "/{artivactId}/image/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getArtivactImage(@PathVariable String artivactId, @PathVariable String fileName,
                                                   @RequestParam(required = false) ImageSize imageSize) {
        if (imageSize == null) {
            imageSize = ImageSize.ORIGINAL;
        }
        byte[] image = mediaService.getArtivactImage(artivactId, fileName, imageSize);
        var responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.valueOf(URLConnection.guessContentTypeFromName(fileName)));
        return new ResponseEntity<>(image, responseHeaders, HttpStatus.OK);
    }

    @GetMapping(value = "/{artivactId}/model/{fileName}", produces = "model/gltf-binary")
    public HttpEntity<byte[]> getArtivactModel(@PathVariable String artivactId, @PathVariable String fileName) {
        var contentDisposition = ContentDisposition.builder("inline")
                .filename(fileName)
                .build();

        var headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        FileSystemResource model = mediaService.getArtivactModel(artivactId, fileName);

        try {
            return new HttpEntity<>(Files.readAllBytes(model.getFile().toPath()), headers);
        } catch (IOException e) {
            throw new ArtivactVaultException("Could not read artivact model!", e);
        }
    }

    @GetMapping(value = "/{artivactId}/zip")
    public ResponseEntity<StreamingResponseBody> downloadMediaZip(HttpServletResponse response,
                                                                  @PathVariable String artivactId) {

        List<String> mediaFiles = mediaService.getMediaFilesForDownload(artivactId);

        StreamingResponseBody streamResponseBody = out -> {
            final ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

            ZipEntry zipEntry = null;
            for (String mediaFile : mediaFiles) {
                File file = new File(mediaFile);
                zipEntry = new ZipEntry(file.getName());

                try (var inputStream = new FileInputStream(file)) {
                    zipOutputStream.putNextEntry(zipEntry);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = inputStream.read(bytes)) >= 0) {
                        zipOutputStream.write(bytes, 0, length);
                    }
                } catch (IOException e) {
                    log.error("Exception while reading and streaming data!", e);
                }

            }

            zipOutputStream.close();

            response.setContentLength((int) (zipEntry != null ? zipEntry.getSize() : 0));
        };

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + artivactId + ".zip");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Expires", "0");

        return ResponseEntity.ok(streamResponseBody);
    }

}
