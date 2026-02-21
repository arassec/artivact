package com.arassec.artivact.application.service.page;

import com.arassec.artivact.application.port.in.item.ImportItemUseCase;
import com.arassec.artivact.application.port.in.page.ImportPageUseCase;
import com.arassec.artivact.application.port.in.page.SavePageContentUseCase;
import com.arassec.artivact.application.port.in.page.UpdatePageAliasUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.widget.ItemSearchWidget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PAGE_EXCHANGE_FILENAME_JSON;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.SEARCH_RESULT_FILENAME_JSON;

/**
 * Service for page import.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PageImportService implements ImportPageUseCase {

    /**
     * The JSON mapper.
     */
    private final JsonMapper jsonMapper;

    /**
     * Repository for file.
     */
    private final FileRepository fileRepository;

    /**
     * Use case for use project dirs.
     */
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Use case for import item.
     */
    private final ImportItemUseCase importItemUseCase;

    /**
     * Use case for save page content.
     */
    private final SavePageContentUseCase savePageContentUseCase;

    /**
     * Use case for update page alias.
     */
    private final UpdatePageAliasUseCase updatePageAliasUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public void importPage(ImportContext importContext, String pageId, String pageAlias) {
        Path pageContentJson = fileRepository.getDirFromId(importContext.getImportDir().resolve(DirectoryDefinitions.PAGES_DIR), pageId)
                .resolve(PAGE_EXCHANGE_FILENAME_JSON);

        PageContent pageContent = jsonMapper.readValue(fileRepository.read(pageContentJson), PageContent.class);

        pageContent.setWidgets(pageContent.getWidgets().stream()
                .filter(Objects::nonNull)
                .toList());

        pageContent.getWidgets().forEach(widget -> {

            // Import Items:
            if (widget instanceof ItemSearchWidget itemSearchWidget) {
                // Import the search result of the widget:
                Path searchResultJson = fileRepository.getDirFromId(importContext.getImportDir()
                                .resolve(DirectoryDefinitions.WIDGETS_DIR), itemSearchWidget.getId())
                        .resolve(SEARCH_RESULT_FILENAME_JSON);
                if (fileRepository.exists(searchResultJson)) {
                    List<String> itemIds = jsonMapper.readValue(fileRepository.read(searchResultJson), new TypeReference<>() {
                    });
                    itemIds.forEach(itemId -> importItemUseCase.importItem(importContext, itemId));
                }
            } else {
                // Import the widget's associated files:
                Path widgetSource = fileRepository.getDirFromId(importContext.getImportDir()
                        .resolve(DirectoryDefinitions.WIDGETS_DIR), widget.getId());

                Path widgetTarget = fileRepository.getDirFromId(useProjectDirsUseCase.getProjectRoot()
                        .resolve(DirectoryDefinitions.WIDGETS_DIR), widget.getId());

                fileRepository.copy(widgetSource, widgetTarget);

            }
        });

        savePageContentUseCase.savePageContent(pageId, Set.of(), pageContent);

        if (StringUtils.hasText(pageAlias)) {
            updatePageAliasUseCase.updatePageAlias(pageId, pageAlias);
        }
    }

}
