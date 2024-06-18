package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.PageService;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.item.ImageSize;
import com.arassec.artivact.backend.service.model.page.PageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

/**
 * REST-Controller for (web-)page management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/page")
public class PageController {

    /**
     * The application's {@link PageService}.
     */
    private final PageService pageService;

    /**
     * Returns the index page.
     *
     * @return The content of the index page.
     */
    @GetMapping()
    public PageContent loadIndexPageContent(Authentication authentication) {
        return pageService.loadIndexPageContent(getRoles(authentication));
    }

    /**
     * Returns the page with the given ID.
     *
     * @param pageId The page's ID.
     * @return The page content.
     */
    @GetMapping("/{pageId}")
    public PageContent loadTranslatedPageContent(@PathVariable String pageId, Authentication authentication) {
        return pageService.loadPageContent(pageId, getRoles(authentication));
    }

    /**
     * Saves a page.
     *
     * @param pageId      The page's ID.
     * @param pageContent The page content to save.
     * @return The updated page content.
     */
    @PostMapping("/{pageId}")
    public PageContent savePageContent(@PathVariable String pageId, @RequestBody PageContent pageContent, Authentication authentication) {
        return pageService.savePageContent(pageId, getRoles(authentication), pageContent);
    }

    /**
     * Saves a file to a widget.
     *
     * @param pageId   The page's ID the widget is on.
     * @param widgetId The widget's ID.
     * @param file     The file to save.
     * @return The filename of the saved file.
     */
    @PostMapping(value = "/{pageId}/widget/{widgetId}")
    public ResponseEntity<String> saveFile(@PathVariable String pageId,
                                           @PathVariable String widgetId,
                                           @RequestPart(value = "file") final MultipartFile file) {
        return ResponseEntity.ok(pageService.saveFile(pageId, widgetId, file));
    }

    /**
     * Loads a widget's file.
     *
     * @param widgetId  The widget's ID.
     * @param filename  The name of the file.
     * @param imageSize Optional target size of an image.
     * @return The file as byte array.
     */
    @GetMapping(value = "/widget/{widgetId}/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<byte[]> loadFile(@PathVariable String widgetId,
                                       @PathVariable String filename,
                                       @RequestParam(required = false) ImageSize imageSize) {

        if (!StringUtils.hasText(filename)) {
            return new HttpEntity<>(new byte[0]);
        }

        var contentDisposition = ContentDisposition.builder("inline")
                .filename(filename)
                .build();

        var headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.valueOf(URLConnection.guessContentTypeFromName(filename)));

        FileSystemResource model = pageService.loadFile(widgetId, filename, imageSize);

        try {
            return new HttpEntity<>(Files.readAllBytes(model.getFile().toPath()), headers);
        } catch (IOException e) {
            throw new ArtivactException("Could not read artivact model!", e);
        }
    }

    /**
     * Extracts the roles of the currently logged in user.
     *
     * @param authentication The Spring-Security Authentication object.
     * @return A set of roles of the user.
     */
    private Set<String> getRoles(Authentication authentication) {
        Set<String> roles = new HashSet<>();
        if (authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            roles.addAll(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        }
        return roles;
    }

}
