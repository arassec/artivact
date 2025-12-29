package com.arassec.artivact.application.service.page;

import com.arassec.artivact.application.port.in.item.ExportItemUseCase;
import com.arassec.artivact.application.port.in.page.ExportPageUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.in.search.SearchItemsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.service.BaseExportService;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.widget.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PAGE_EXCHANGE_FILE_SUFFIX;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.SEARCH_RESULT_FILE_SUFFIX;

/**
 * Service for page export.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PageExportService extends BaseExportService implements ExportPageUseCase {

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
     * Use case for export item.
     */
    private final ExportItemUseCase exportItemUseCase;

    /**
     * Use case for search items.
     */
    private final SearchItemsUseCase searchItemsUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportPage(ExportContext exportContext, String targetPageId, PageContent pageContent) {
        if (exportContext.getExportConfiguration().isApplyRestrictions() && !pageContent.getRestrictions().isEmpty()) {
            return;
        }

        pageContent.getWidgets().forEach(widget -> {
            if (exportContext.getExportConfiguration().isApplyRestrictions() && !widget.getRestrictions().isEmpty()) {
                return;
            }
            exportWidget(exportContext, widget);
        });

        writeJsonFile(exportContext.getExportDir().resolve(targetPageId + PAGE_EXCHANGE_FILE_SUFFIX), pageContent);
    }

    /**
     * Exports the supplied widget.
     *
     * @param exportContext Context of the content export.
     * @param widget        The widget to export.
     */
    private void exportWidget(ExportContext exportContext, Widget widget) {
        Optional.ofNullable(widget.getNavigationTitle()).ifPresent(title -> title.setTranslatedValue(null));
        switch (widget) {
            case AvatarWidget avatarWidget -> {
                cleanupTranslations(avatarWidget.getAvatarSubtext());
                copyWidgetFile(exportContext, avatarWidget, avatarWidget.getAvatarImage());
            }
            case InfoBoxWidget infoBoxWidget -> {
                cleanupTranslations(infoBoxWidget.getHeading());
                cleanupTranslations(infoBoxWidget.getContent());
            }
            case PageTitleWidget pageTitleWidget -> {
                cleanupTranslations(pageTitleWidget.getTitle());
                copyWidgetFile(exportContext, pageTitleWidget, pageTitleWidget.getBackgroundImage());
            }
            case ItemSearchWidget itemSearchWidget -> {
                cleanupTranslations(itemSearchWidget.getHeading());
                cleanupTranslations(itemSearchWidget.getContent());
                exportItemSearchWidgetsItems(exportContext, itemSearchWidget);
            }
            case TextWidget textWidget -> {
                cleanupTranslations(textWidget.getHeading());
                cleanupTranslations(textWidget.getContent());
            }
            case ImageGalleryWidget imageGalleryWidget -> {
                cleanupTranslations(imageGalleryWidget.getHeading());
                cleanupTranslations(imageGalleryWidget.getContent());
                imageGalleryWidget.getImages().forEach(image -> copyWidgetFile(exportContext, imageGalleryWidget, image));
            }
            default -> log.warn("Unknown widget type for export: {}", widget.getType());
        }
    }

    /**
     * Exports an {@link ItemSearchWidget}'s item list if required.
     *
     * @param exportContext    The export context.
     * @param itemSearchWidget The widget to export the item list for.
     */
    private void exportItemSearchWidgetsItems(ExportContext exportContext, ItemSearchWidget itemSearchWidget) {
        if (!exportContext.getExportConfiguration().isExcludeItems()) {
            String searchTerm = itemSearchWidget.getSearchTerm();
            int maxResults = itemSearchWidget.getMaxResults();
            List<Item> searchResult = searchItemsUseCase.search(searchTerm, maxResults);
            if (searchResult != null && !searchResult.isEmpty()) {
                Set<String> excludedItemIds = new HashSet<>();
                for (Item item : searchResult) {
                    if (exportContext.getExportConfiguration().isApplyRestrictions() && !item.getRestrictions().isEmpty()) {
                        excludedItemIds.add(item.getId());
                        continue;
                    }
                    exportItemUseCase.exportItem(exportContext, item);
                }
                writeJsonFile(exportContext.getExportDir().resolve(itemSearchWidget.getId() + SEARCH_RESULT_FILE_SUFFIX),
                        searchResult.stream().map(Item::getId).filter(itemId -> !excludedItemIds.contains(itemId)).toArray());
            }
        }
    }

    /**
     * Copies a widget file to the widget's export directory.
     *
     * @param exportContext Export context.
     * @param widget        The widget to copy files from.
     * @param file          The file to copy.
     */
    private void copyWidgetFile(ExportContext exportContext, Widget widget, String file) {
        if (StringUtils.hasText(file)) {
            Path sourceDir = fileRepository.getDirFromId(useProjectDirsUseCase.getProjectRoot().resolve(DirectoryDefinitions.WIDGETS_DIR), widget.getId());
            Path targetDir = exportContext.getExportDir().resolve(widget.getId());
            fileRepository.createDirIfRequired(targetDir);
            fileRepository.copy(sourceDir.resolve(file), targetDir.resolve(file));
        }
    }

}
