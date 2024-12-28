package com.arassec.artivact.domain.exchange.exp;

import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.widget.*;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.SEARCH_RESULT_FILE_SUFFIX;

/**
 * Exporter for {@link Widget}s.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WidgetExporter extends BaseExporter {

    /**
     * The application's object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * The service for searching items.
     */
    private final SearchService searchService;

    /**
     * The application's file repository.
     */
    private final FileRepository fileRepository;

    /**
     * Exporter for items.
     */
    private final ItemExporter itemExporter;

    /**
     * The application's project data provider.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * Exports the supplied widget.
     *
     * @param exportContext Context of the content export.
     * @param widget        The widget to export.
     */
    public void exportWidget(ExportContext exportContext, Widget widget) {
        Optional.ofNullable(widget.getNavigationTitle()).ifPresent(title -> title.setTranslatedValue(null));
        switch (widget) {
            case AvatarWidget avatarWidget -> {
                Optional.ofNullable(avatarWidget.getAvatarSubtext()).ifPresent(subtext -> subtext.setTranslatedValue(null));
                copyWidgetFile(exportContext, avatarWidget, avatarWidget.getAvatarImage());
            }
            case ImageTextWidget imageTextWidget -> {
                Optional.ofNullable(imageTextWidget.getText()).ifPresent(text -> text.setTranslatedValue(null));
                copyWidgetFile(exportContext, imageTextWidget, imageTextWidget.getImage());
            }
            case InfoBoxWidget infoBoxWidget -> {
                Optional.ofNullable(infoBoxWidget.getHeading()).ifPresent(heading -> heading.setTranslatedValue(null));
                Optional.ofNullable(infoBoxWidget.getContent()).ifPresent(content -> content.setTranslatedValue(null));
            }
            case PageTitleWidget pageTitleWidget -> {
                Optional.ofNullable(pageTitleWidget.getTitle()).ifPresent(title -> title.setTranslatedValue(null));
                copyWidgetFile(exportContext, pageTitleWidget, pageTitleWidget.getBackgroundImage());
            }
            case ItemSearchWidget itemSearchWidget -> {
                Optional.ofNullable(itemSearchWidget.getHeading()).ifPresent(heading -> heading.setTranslatedValue(null));
                Optional.ofNullable(itemSearchWidget.getContent()).ifPresent(content -> content.setTranslatedValue(null));
                exportItemSearchWidgetsItems(exportContext, itemSearchWidget);
            }
            case TextWidget textWidget -> {
                Optional.ofNullable(textWidget.getHeading()).ifPresent(heading -> heading.setTranslatedValue(null));
                Optional.ofNullable(textWidget.getContent()).ifPresent(content -> content.setTranslatedValue(null));
            }
            default -> log.info("No export available for widget type: {}", widget.getType());
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
            List<Item> searchResult = searchService.search(searchTerm, maxResults);
            if (searchResult != null && !searchResult.isEmpty()) {
                for (Item item : searchResult) {
                    if (exportContext.getExportConfiguration().isApplyRestrictions() && !item.getRestrictions().isEmpty()) {
                        continue;
                    }
                    itemExporter.exportItem(exportContext, item);
                }
                writeJsonFile(exportContext.getExportDir().resolve(itemSearchWidget.getId() + SEARCH_RESULT_FILE_SUFFIX),
                        searchResult.stream().map(Item::getId).toArray());
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
            Path sourceDir = fileRepository.getDirFromId(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.WIDGETS_DIR), widget.getId());
            Path targetDir = exportContext.getExportDir().resolve(widget.getId());
            fileRepository.createDirIfRequired(targetDir);
            fileRepository.copy(sourceDir.resolve(file), targetDir.resolve(file));
        }
    }

}
