package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.model.item.ImageSize;
import com.arassec.artivact.core.model.page.Page;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.domain.service.PageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLConnection;
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
     * Returns the alias or ID of the index page.
     *
     * @return The alias or ID of the index page.
     */
    @GetMapping()
    public String loadIndexPageIdOrAlias(Authentication authentication) {
        Page indexPage = pageService.loadIndexPage(getRoles(authentication));
        if (indexPage == null) {
            return "";
        } else if (StringUtils.hasText(indexPage.getAlias())) {
            return indexPage.getAlias();
        }
        return indexPage.getId();
    }

    /**
     * Returns the page with the given ID.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @return The page content.
     */
    @GetMapping("/{pageIdOrAlias}")
    public PageContent loadTranslatedPageContent(@PathVariable String pageIdOrAlias, Authentication authentication) {
        return pageService.loadTranslatedRestrictedPageContent(pageIdOrAlias, getRoles(authentication));
    }

    /**
     * Saves a page.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @param pageContent   The page content to save.
     * @return The updated page content.
     */
    @PostMapping("/{pageIdOrAlias}")
    public PageContent savePageContent(@PathVariable String pageIdOrAlias, @RequestBody PageContent pageContent, Authentication authentication) {
        return pageService.savePageContent(pageIdOrAlias, getRoles(authentication), pageContent);
    }

    /**
     * Saves a file to a widget.
     *
     * @param pageIdOrAlias The page's ID or alias the widget is on.
     * @param widgetId      The widget's ID.
     * @param file          The file to save.
     * @return The filename of the saved file.
     */
    @PostMapping(value = "/{pageIdOrAlias}/widget/{widgetId}")
    public ResponseEntity<String> saveFile(@PathVariable String pageIdOrAlias,
                                           @PathVariable String widgetId,
                                           @RequestPart(value = "file") final MultipartFile file) {
        return ResponseEntity.ok(pageService.saveFile(pageIdOrAlias, widgetId, file));
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

        byte[] model = pageService.loadFile(widgetId, filename, imageSize);

        return new HttpEntity<>(model, headers);
    }

    /**
     * Extracts the roles of the currently logged-in user.
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
