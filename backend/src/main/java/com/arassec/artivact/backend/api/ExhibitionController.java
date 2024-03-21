package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.ExhibitionSummary;
import com.arassec.artivact.backend.service.ExhibitionService;
import com.arassec.artivact.backend.service.model.exhibition.Exhibition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller for exhibition management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/exhibition")
public class ExhibitionController extends BaseController {

    /**
     * The application's {@link ExhibitionService}.
     */
    private final ExhibitionService exhibitionService;

    /**
     * Returns the list of available exhibitions.
     *
     * @return The list of exhibitions.
     */
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

    /**
     * Creates a new exhibition or updates an existing one based on the given configuration.
     *
     * @param exhibitionSummary The configuration of the exhibition.
     * @return The created/updated exhibition.
     */
    @PostMapping()
    public ResponseEntity<Exhibition> save(@RequestBody ExhibitionSummary exhibitionSummary) {
        return ResponseEntity.ok(exhibitionService.save(exhibitionSummary.getExhibitionId(),
                exhibitionSummary.getTitle(), exhibitionSummary.getDescription(), exhibitionSummary.getMenuIds()));
    }

    /**
     * Deletes an exhibition.
     *
     * @param exhibitionId The exhibition's ID.
     */
    @DeleteMapping("/{exhibitionId}")
    public void delete(@PathVariable String exhibitionId) {
        exhibitionService.delete(exhibitionId);
    }

}
