package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.ExhibitionSummary;
import com.arassec.artivact.backend.service.ExhibitionService;
import com.arassec.artivact.backend.service.model.exhibition.Exhibition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/exhibition")
public class ExhibitionController extends BaseController {

    private final ExhibitionService exhibitionService;

    @GetMapping
    public ResponseEntity<List<ExhibitionSummary>> read() {
        return ResponseEntity.ok(exhibitionService.loadAll().stream()
                .map(exhibition -> ExhibitionSummary.builder()
                        .exhibitionId(exhibition.getId())
                        .title(exhibition.getTitle())
                        .description(exhibition.getDescription())
                        .menuIds(exhibition.getReferencedMenuIds())
                        .build())
                .toList());
    }

    @PostMapping()
    public ResponseEntity<Exhibition> save(@RequestBody ExhibitionSummary exhibitionSummary) {
        return ResponseEntity.ok(exhibitionService.save(exhibitionSummary.getExhibitionId(),
                exhibitionSummary.getTitle(), exhibitionSummary.getDescription(), exhibitionSummary.getMenuIds()));
    }

    @DeleteMapping("/{exhibitionId}")
    public void delete(@PathVariable String exhibitionId) {
        exhibitionService.delete(exhibitionId);
    }

}
