package com.arassec.artivact.domain.exchange.exporter;

import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.arassec.artivact.domain.service.PageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.MENU_EXCHANGE_FILE_SUFFIX;

/**
 * Exporter for {@link Menu}s.
 */
@Component
@RequiredArgsConstructor
public class MenuExporter extends BaseExporter {

    /**
     * The application's object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * Exporter for pages.
     */
    private final PageExporter pageExporter;

    /**
     * The service for page handling.
     */
    private final PageService pageService;

    /**
     * Exports a menu.
     *
     * @param exportContext Context of the export.
     * @param menu          The menu to export.
     */
    public void exportMenu(ExportContext exportContext, Menu menu) {
        // Only restrict export for submenus, not the main-menu.
        if (exportContext.getExportConfiguration().isApplyRestrictions()) {
            menu.setMenuEntries(menu.getMenuEntries().isEmpty() ? null : menu.getMenuEntries().stream()
                    .filter(menuEntry -> menuEntry.getRestrictions().isEmpty())
                    .toList());
        }

        cleanupTranslations(menu);
        Optional.ofNullable(menu.getMenuEntries()).orElse(List.of()).forEach(this::cleanupTranslations);

        writeJsonFile(exportContext.getExportDir().resolve(menu.getId() + MENU_EXCHANGE_FILE_SUFFIX), menu);

        // Export (previously filtered) menu entries:
        Optional.ofNullable(menu.getMenuEntries()).orElse(List.of())
                .forEach(menuEntry -> exportMenu(exportContext, menuEntry));

        if (StringUtils.hasText(menu.getTargetPageId())) {
            var pageContent = pageService.loadPageContent(menu.getTargetPageId(), Set.of(Roles.ROLE_ADMIN, Roles.ROLE_USER));
            pageExporter.exportPage(exportContext, menu.getTargetPageId(), pageContent);
        }
    }

}
