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

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PAGE_EXCHANGE_FILE_SUFFIX;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.SEARCH_RESULT_FILE_SUFFIX;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Service for page import.
 */
public class PageImportService implements ImportPageUseCase {

    /**
     * The json mapper.
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
        Path pageContentJson = importContext.getImportDir().resolve(pageId + PAGE_EXCHANGE_FILE_SUFFIX);

        PageContent pageContent = jsonMapper.readValue(fileRepository.read(pageContentJson), PageContent.class);

        pageContent.setWidgets(pageContent.getWidgets().stream()
                .filter(Objects::nonNull)
                .toList());

        pageContent.getWidgets().forEach(widget -> {
            // Import the widget:
            Path widgetSource = importContext.getImportDir().resolve(widget.getId());

            Path widgetTarget = fileRepository.getDirFromId(useProjectDirsUseCase.getProjectRoot()
                    .resolve(DirectoryDefinitions.WIDGETS_DIR), widget.getId());

            fileRepository.copy(widgetSource, widgetTarget);

            // Import Items:
            if (widget instanceof ItemSearchWidget itemSearchWidget) {
                Path searchResultJson = importContext.getImportDir().resolve(itemSearchWidget.getId() + SEARCH_RESULT_FILE_SUFFIX);
                if (fileRepository.exists(searchResultJson)) {
                    List<String> itemIds = jsonMapper.readValue(fileRepository.read(searchResultJson), new TypeReference<>() {
                    });
                    itemIds.forEach(itemId -> importItemUseCase.importItem(importContext, itemId));
                }
            }
        });

        savePageContentUseCase.savePageContent(pageId, Set.of(), pageContent);

        if (StringUtils.hasText(pageAlias)) {
            updatePageAliasUseCase.updatePageAlias(pageId, pageAlias);
        }
    }

}
