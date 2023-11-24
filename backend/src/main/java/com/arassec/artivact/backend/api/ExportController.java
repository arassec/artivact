package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.PageService;
import com.arassec.artivact.backend.service.VaultExhibitionExportService;
import com.arassec.artivact.backend.service.model.page.PageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final VaultExhibitionExportService vaultExhibitionExportService;

    private final PageService pageService;

    @PostMapping("/av-exhibition")
    public ResponseEntity<String> exportPage(@RequestBody String pageId) {
        PageContent pageContent = pageService.loadPageContent(pageId);
        vaultExhibitionExportService.exportPage(pageId, pageContent);
        return ResponseEntity.ok("exported");
    }

}
