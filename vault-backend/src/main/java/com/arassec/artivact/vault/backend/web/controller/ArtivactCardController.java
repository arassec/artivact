package com.arassec.artivact.vault.backend.web.controller;

import com.arassec.artivact.vault.backend.service.ArtivactService;
import com.arassec.artivact.vault.backend.web.model.ArtivactCardData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/artivact/card")
public class ArtivactCardController extends BaseController {

    private final ArtivactService artivactService;

    @GetMapping
    public List<ArtivactCardData> loadCards(Locale locale, Authentication authentication) {
        return artivactService.loadArtivacts(getRoles(authentication)).stream()
                .map(artivact -> ArtivactCardData.builder()
                        .artivactId(artivact.getId())
                        .title(artivact.getTitle().getTranslatedValue(locale.toString()))
                        .imageUrl(createMainImageUrl(artivact))
                        .build()
                )
                .toList();
    }

}
