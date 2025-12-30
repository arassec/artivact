package com.arassec.artivact.application.service.menu;

import com.arassec.artivact.application.port.in.menu.ExportMenuUseCase;
import com.arassec.artivact.application.port.in.menu.LoadMenuUseCase;
import com.arassec.artivact.application.port.in.page.ExportPageUseCase;
import com.arassec.artivact.application.port.in.page.LoadPageContentUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.service.BaseExportService;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExportConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.menu.Menu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.MENU_EXCHANGE_FILE_SUFFIX;

/**
 * Service for menu export.
 */
@Service
@RequiredArgsConstructor
public class MenuExportService extends BaseExportService implements ExportMenuUseCase {

    /**
     * The json mapper.
     */
    @Getter
    private final JsonMapper jsonMapper;

    /**
     * Repository for file.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * Use case for use project dirs.
     */
    @Getter
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Use case for load page content.
     */
    private final LoadPageContentUseCase loadPageContentUseCase;

    /**
     * Use case for export page.
     */
    private final ExportPageUseCase exportPageUseCase;

    /**
     * Use case for load menu.
     */
    private final LoadMenuUseCase loadMenuUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportMenu(String menuId) {
        Menu menu = loadMenuUseCase.loadMenu(menuId);

        ExportContext exportContext = createExportContext(menu.getId(), ExportConfiguration.builder()
                .applyRestrictions(false)
                .optimizeSize(false)
                .excludeItems(true)
                .build());

        prepareExport(exportContext);

        exportMainData(exportContext, ContentSource.MENU, menu.getId(), null, null, null);

        exportMenu(exportContext, menu);

        fileRepository.pack(exportContext.getExportDir(), exportContext.getExportFile());
        fileRepository.delete(exportContext.getExportDir());

        return exportContext.getExportFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            var pageContent = loadPageContentUseCase.loadPageContent(menu.getTargetPageId(), Set.of(Roles.ROLE_ADMIN, Roles.ROLE_USER));
            exportPageUseCase.exportPage(exportContext, menu.getTargetPageId(), pageContent);
        }
    }

}
