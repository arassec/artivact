package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.PageService;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.page.PageContent;
import com.arassec.artivact.backend.service.model.item.ImageSize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/page")
public class PageController {

    private final PageService pageService;

    @GetMapping()
    public PageContent loadIndexPageContent() {
        return pageService.loadIndexPageContent();
    }

    @GetMapping("/{pageId}")
    public PageContent loadTranslatedPageContent(@PathVariable String pageId) {
        return pageService.loadPageContent(pageId);
    }

    @PostMapping("/{pageId}")
    public PageContent savePageContent(@PathVariable String pageId, @RequestBody PageContent pageContent) {
        return pageService.savePageContent(pageId, pageContent);
    }

    @PostMapping(value = "/{pageId}/widget/{widgetId}")
    public ResponseEntity<String> saveFile(@PathVariable String pageId,
                                           @PathVariable String widgetId,
                                           @RequestPart(value = "file") final MultipartFile file) {
        return ResponseEntity.ok(pageService.saveFile(pageId, widgetId, file));
    }

    @GetMapping(value = "/widget/{widgetId}/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<byte[]> loadFile(@PathVariable String widgetId,
                                       @PathVariable String fileName,
                                       @RequestParam(required = false) ImageSize imageSize) {

        var contentDisposition = ContentDisposition.builder("inline")
                .filename(fileName)
                .build();

        var headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.valueOf(URLConnection.guessContentTypeFromName(fileName)));

        FileSystemResource model = pageService.loadFile(widgetId, fileName, imageSize);

        try {
            return new HttpEntity<>(Files.readAllBytes(model.getFile().toPath()), headers);
        } catch (IOException e) {
            throw new ArtivactException("Could not read artivact model!", e);
        }
    }

}
