package com.arassec.artivact.vault.backend.web.controller;

import com.arassec.artivact.vault.backend.service.ArtivactService;
import com.arassec.artivact.vault.backend.service.model.MediaContent;
import com.arassec.artivact.vault.backend.service.model.Tag;
import com.arassec.artivact.vault.backend.service.model.VaultArtivact;
import com.arassec.artivact.vault.backend.web.model.ArtivactDetails;
import com.arassec.artivact.vault.backend.web.model.MediaEntry;
import com.arassec.artivact.vault.backend.web.model.TranslatedTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/artivact")
public class ArtivactController extends BaseController {

    private final ArtivactService artivactService;

    @GetMapping( "/{artivactId}")
    public ResponseEntity<ArtivactDetails> loadArtivact(@PathVariable String artivactId, Locale locale,
                                                        Authentication authentication) {
        VaultArtivact artivact = artivactService.loadArtivact(artivactId, getRoles(authentication));

        if (artivact != null) {
            return ResponseEntity.ok(ArtivactDetails.builder()
                    .id(artivact.getId())
                    .version(artivact.getVersion())
                    .title(translateItem(artivact.getTitle(), locale))
                    .description(translateItem(artivact.getDescription(), locale))
                    .images(artivact.getMediaContent().getImages().stream()
                            .map(fileName -> MediaEntry.builder()
                                    .fileName(fileName)
                                    .url(createImageUrl(artivactId, fileName))
                                    .build())
                            .toList())
                    .models(artivact.getMediaContent().getModels().stream()
                            .map(fileName -> MediaEntry.builder()
                                    .fileName(fileName)
                                    .url(createModelUrl(artivactId, fileName))
                                    .build())
                            .toList())
                    .properties(artivact.getProperties())
                    .tags(artivact.getTags().stream()
                            .map(tag -> {
                                TranslatedTag translatedTag = new TranslatedTag();
                                translatedTag.setId(tag.getId());
                                translatedTag.setTranslatedValue(tag.getTranslatedValue(locale.toString()));
                                translatedTag.setUrl(tag.getUrl());
                                return translatedTag;
                            })
                            .toList())
                    .build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Void> saveArtivact(@RequestBody ArtivactDetails artivactDetails,
                                             Authentication authentication) {

        if (!isAdminOrUser(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MediaContent mediaContent = new MediaContent();
        mediaContent.getImages().addAll(artivactDetails.getImages().stream()
                .map(MediaEntry::getFileName)
                .toList());
        mediaContent.getModels().addAll(artivactDetails.getModels().stream()
                .map(MediaEntry::getFileName)
                .toList());

        VaultArtivact vaultArtivact = artivactService.loadArtivact(artivactDetails.getId(), getRoles(authentication));
        if (vaultArtivact == null) {
            vaultArtivact = artivactService.createArtivact();
            vaultArtivact.setId(artivactDetails.getId());
        }

        vaultArtivact.setVersion(artivactDetails.getVersion());
        vaultArtivact.setTitle(removeTranslation(artivactDetails.getTitle()));
        vaultArtivact.setDescription(removeTranslation(artivactDetails.getDescription()));
        vaultArtivact.setMediaContent(mediaContent);
        vaultArtivact.setProperties(artivactDetails.getProperties());
        vaultArtivact.setTags(artivactDetails.getTags().stream()
                .map(translatedTag -> {
                    Tag tag = new Tag();
                    tag.setId(translatedTag.getId());
                    return tag;
                })
                .toList());

        artivactService.saveArtivact(vaultArtivact);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping( "/{artivactId}")
    public ResponseEntity<Void> deleteArtivact(@PathVariable String artivactId, Authentication authentication) {

        if (!isAdminOrUser(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        artivactService.deleteArtivact(artivactId, getRoles(authentication));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/new")
    public ResponseEntity<String> createArtivact(Authentication authentication) {
        if (!isAdminOrUser(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        VaultArtivact artivact = artivactService.createArtivact();

        return ResponseEntity.ok(artivact.getId());
    }

}
