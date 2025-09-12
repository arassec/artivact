package com.arassec.artivact.adapter.in.rest.controller.page;

import com.arassec.artivact.application.port.in.page.*;
import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.PageIdAndAlias;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * REST-Controller for (web-)page management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/page")
public class PageController {

    private final LoadPageContentUseCase loadPageContentUseCase;

    private final SavePageContentUseCase savePageContentUseCase;

    private final ManagePageMediaUseCase managePageMediaUseCase;

    private final ResetWipPageContentUseCase resetWipPageContentUseCase;

    private final PublishWipPageContentUseCase publishWipPageContentUseCase;

    @GetMapping("/id")
    public List<PageIdAndAlias> getPageIds() {
        return loadPageContentUseCase.loadPageIds();
    }

    /**
     * Returns the alias or ID of the index page.
     *
     * @return The alias or ID of the index page.
     */
    @GetMapping()
    public String loadIndexPageIdOrAlias() {
        Optional<PageIdAndAlias> pageIdAndAliasOptional = loadPageContentUseCase.loadIndexPageIdAndAlias();
        if (pageIdAndAliasOptional.isEmpty()) {
            return "";
        }
        PageIdAndAlias pageIdAndAlias = pageIdAndAliasOptional.get();
        if (StringUtils.hasText(pageIdAndAlias.getAlias())) {
            return pageIdAndAlias.getAlias();
        }
        return pageIdAndAlias.getId();
    }

    /**
     * Returns the page with the given ID.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @return The page content.
     */
    @GetMapping("/{pageIdOrAlias}")
    public PageContent loadTranslatedPageContent(@PathVariable String pageIdOrAlias, Authentication authentication) {
        return loadPageContentUseCase.loadTranslatedRestrictedPageContent(pageIdOrAlias, getRoles(authentication));
    }

    /**
     * Returns the 'work-in-progress' page with the given ID.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @return The page content.
     */
    @GetMapping("/{pageIdOrAlias}/wip")
    public PageContent loadWipPageContent(@PathVariable String pageIdOrAlias, Authentication authentication) {
        return loadPageContentUseCase.loadTranslatedRestrictedWipPageContent(pageIdOrAlias, getRoles(authentication));
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
        return savePageContentUseCase.savePageContent(pageIdOrAlias, getRoles(authentication), pageContent);
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
        synchronized (this) {
            return ResponseEntity.ok(managePageMediaUseCase.saveFile(pageIdOrAlias, widgetId, file));
        }
    }

    /**
     * Deletes a file from a widget.
     *
     * @param pageIdOrAlias The page's ID or alias the widget is on.
     * @param widgetId      The widget's ID.
     * @param filename      The name of the file to delete.
     */
    @DeleteMapping(value = "/{pageIdOrAlias}/widget/{widgetId}/{filename}")
    public PageContent deleteFile(@PathVariable String pageIdOrAlias,
                                  @PathVariable String widgetId,
                                  @PathVariable String filename) {
        synchronized (this) {
            return managePageMediaUseCase.deleteFile(pageIdOrAlias, widgetId, filename);
        }
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

        byte[] model = managePageMediaUseCase.loadFile(widgetId, filename, imageSize, false);

        return new HttpEntity<>(model, headers);
    }

    /**
     * Loads a widget's 'work-in-progress' file.
     *
     * @param widgetId  The widget's ID.
     * @param filename  The name of the file.
     * @param imageSize Optional target size of an image.
     * @return The file as byte array.
     */
    @GetMapping(value = "/widget/{widgetId}/{filename}/wip", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<byte[]> loadWipFile(@PathVariable String widgetId,
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

        byte[] model = managePageMediaUseCase.loadFile(widgetId, filename, imageSize, true);

        return new HttpEntity<>(model, headers);
    }

    /**
     * Resets the 'work-in-progress' state of a page to its published one.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @return The updated page content.
     */
    @PostMapping("/reset-wip/{pageIdOrAlias}")
    public PageContent resetWipPageContent(@PathVariable String pageIdOrAlias) {
        return resetWipPageContentUseCase.resetWipPageContent(pageIdOrAlias);
    }

    /**
     * Publishes the 'work-in-progress' state of a page.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @return The updated page content.
     */
    @PostMapping("/publish-wip/{pageIdOrAlias}")
    public PageContent publishWipPageContent(@PathVariable String pageIdOrAlias) {
        return publishWipPageContentUseCase.publishWipPageContent(pageIdOrAlias);
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
