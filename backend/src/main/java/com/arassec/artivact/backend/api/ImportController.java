package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportService importService;

    @PostMapping
    public ResponseEntity<String> importItems() {
        importService.importItems();
        return ResponseEntity.ok("scanned");
    }

}
