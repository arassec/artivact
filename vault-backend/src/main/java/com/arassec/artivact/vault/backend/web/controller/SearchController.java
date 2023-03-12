package com.arassec.artivact.vault.backend.web.controller;

import com.arassec.artivact.vault.backend.service.SearchService;
import com.arassec.artivact.vault.backend.service.model.search.ArtivactSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/artivact/search")
public class SearchController extends BaseController {

    private final SearchService searchService;

    @GetMapping
    public ArtivactSearchResult search(@RequestParam("query") String searchTerm,
                                       @RequestParam("pageNo") int pageNumber,
                                       @RequestParam("pageSize") int pageSize,
                                       Authentication authentication) {
        return searchService.search(getRoles(authentication), searchTerm.toLowerCase(), pageNumber, pageSize);
    }

}
