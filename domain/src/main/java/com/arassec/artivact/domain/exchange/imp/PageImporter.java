package com.arassec.artivact.domain.exchange.imp;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.page.Page;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.model.page.widget.ItemSearchWidget;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.PageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.PAGE_EXCHANGE_FILE_SUFFIX;
import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.SEARCH_RESULT_FILE_SUFFIX;

/**
 * Importer for {@link Page}s.
 */
@Component
@RequiredArgsConstructor
public class PageImporter {

    /**
     * Provider for project data.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * The application's {@link FileRepository}.
     */
    private final FileRepository fileRepository;

    /**
     * The object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Service for page handling.
     */
    private final PageService pageService;

    /**
     * Service for item handling.
     */
    private final ItemImporter itemImporter;

    /**
     * Imports a page.
     *
     * @param importContext The import context.
     * @param pageId        The page's ID.
     */
    public void importPage(ImportContext importContext, String pageId) {
        Path pageContentJson = importContext.getImportDir().resolve(pageId + PAGE_EXCHANGE_FILE_SUFFIX);

        try {
            PageContent pageContent = objectMapper.readValue(fileRepository.read(pageContentJson), PageContent.class);

            pageContent.getWidgets().forEach(widget -> {
                // Import the widget:
                Path widgetSource = importContext.getImportDir().resolve(widget.getId());
                Path widgetTarget = projectDataProvider.getProjectRoot()
                        .resolve(ProjectDataProvider.WIDGETS_DIR)
                        .resolve(fileRepository.getSubDir(widget.getId(), 0))
                        .resolve(fileRepository.getSubDir(widget.getId(), 1))
                        .resolve(widget.getId());
                fileRepository.copyDir(widgetSource, widgetTarget);

                // Import Items:
                if (widget instanceof ItemSearchWidget itemSearchWidget) {
                    Path searchResultJson = importContext.getImportDir().resolve(itemSearchWidget.getId() + SEARCH_RESULT_FILE_SUFFIX);
                    if (fileRepository.exists(searchResultJson)) {
                        try {
                            List<String> itemIds = objectMapper.readValue(fileRepository.read(searchResultJson), new TypeReference<>() {
                            });
                            itemIds.forEach(itemId -> itemImporter.importItem(importContext, itemId));
                        } catch (JsonProcessingException e) {
                            throw new ArtivactException("Could not read search result!", e);
                        }
                    }
                }
            });

            pageService.savePageContent(pageId, Set.of(), pageContent);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not import page!", e);
        }
    }

}
