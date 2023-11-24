package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.service.exception.VaultException;
import com.arassec.artivact.backend.service.model.export.avexhibition.ArtivactExhibitionModule;
import com.arassec.artivact.backend.service.model.export.avexhibition.ItemData;
import com.arassec.artivact.backend.service.model.export.avexhibition.TranslatableText;
import com.arassec.artivact.backend.service.model.export.avexhibition.module.ItemListModule;
import com.arassec.artivact.backend.service.model.export.avexhibition.module.TextModule;
import com.arassec.artivact.backend.service.model.export.avexhibition.module.TitleModule;
import com.arassec.artivact.backend.service.model.page.PageContent;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.widget.ItemCarouselWidget;
import com.arassec.artivact.backend.service.model.page.widget.PageTitleWidget;
import com.arassec.artivact.backend.service.model.page.widget.SearchWidget;
import com.arassec.artivact.backend.service.model.page.widget.TextWidget;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VaultExhibitionExportService extends BaseFileService {

    private static final String EXPORT_DIR = "exports";

    private static final String EXPORT_ITEMS_DIR = "items";

    private static final String EXPORT_MODULES_DIR = "modules";

    private static final String EXPORT_INDEX_FILE = "index.json";

    private final SearchService searchService;

    private final ProjectRootProvider projectRootProvider;

    @Getter
    private final ObjectMapper objectMapper;

    public void exportPage(String pageId, PageContent pageContent) {

        Path exportPath = projectRootProvider.getProjectRoot().resolve(EXPORT_DIR).resolve(pageId);
        deleteDirRecursively(exportPath);
        createDirIfRequired(exportPath);

        Path itemsExportDir = exportPath.resolve(EXPORT_ITEMS_DIR);
        createDirIfRequired(itemsExportDir);

        Path modulesExportDir = exportPath.resolve(EXPORT_MODULES_DIR);
        createDirIfRequired(modulesExportDir);

        Path itemsDir = projectRootProvider.getProjectRoot().resolve(ITEMS_FILE_DIR);
        Path widgetsDir = projectRootProvider.getProjectRoot().resolve(PageService.WIDGETS_FILE_DIR);

        // Map AVV-Widgets to AVE-Modules:
        List<ArtivactExhibitionModule> modules = pageContent.getWidgets().stream()
                .map(this::toVaultExhibitionModule)
                .filter(Objects::nonNull)
                .toList();

        // Create index file:
        try {
            Files.writeString(exportPath.resolve(EXPORT_INDEX_FILE),
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(modules), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new VaultException("Could not write export index.json!", e);
        }

        // Copy required module and item files:
        modules.forEach(module -> {
            try {
                if (module instanceof TitleModule titleModule && StringUtils.hasText(titleModule.getImage())) {
                    Path moduleDir = modulesExportDir.resolve(titleModule.getId());
                    Files.createDirectory(moduleDir);
                    Files.copy(getDirFromId(widgetsDir, titleModule.getId()).resolve(titleModule.getImage()), moduleDir.resolve(titleModule.getImage()));
                } else if (module instanceof ItemListModule itemListModule) {
                    for (ItemData itemData : itemListModule.getItemData()) {
                        Path itemExportDir = itemsExportDir.resolve(itemData.getId());
                        Files.createDirectory(itemExportDir);
                        Files.copy(getDirFromId(itemsDir, itemData.getId()).resolve(MODELS_DIR).resolve(itemData.getModelFile()), itemExportDir.resolve(itemData.getModelFile()));
                    }
                }
            } catch (IOException e) {
                throw new VaultException("Could not export file!", e);
            }
        });

    }

    private ArtivactExhibitionModule toVaultExhibitionModule(Widget widget) {
        ArtivactExhibitionModule result = null;
        switch (widget.getType()) {
            case PAGE_TITLE -> {
                PageTitleWidget ptw = (PageTitleWidget) widget;
                result = new TitleModule(ptw.getId(),
                        new TranslatableText(ptw.getTitle().getValue(), ptw.getTitle().getTranslations()),
                        ptw.getBackgroundImage());
            }
            case TEXT -> {
                TextWidget tw = (TextWidget) widget;
                result = new TextModule(tw.getId(),
                        new TranslatableText(tw.getHeading().getValue(), tw.getHeading().getTranslations()),
                        new TranslatableText(tw.getContent().getValue(), tw.getContent().getTranslations()));
            }
            case ITEM_CAROUSEL -> {
                ItemCarouselWidget icw = (ItemCarouselWidget) widget;
                List<ItemData> itemData = getItemIds(icw.getSearchTerm(), icw.getMaxResults());
                if (!itemData.isEmpty()) {
                    result = new ItemListModule(widget.getId(), itemData);
                }
            }
            case SEARCH -> {
                SearchWidget sw = (SearchWidget) widget;
                List<ItemData> itemData = getItemIds(sw.getSearchTerm(), sw.getMaxResults());
                if (!itemData.isEmpty()) {
                    result = new ItemListModule(widget.getId(), itemData);
                }
            }
        }
        return result;
    }

    private List<ItemData> getItemIds(String searchTerm, int maxResults) {
        return searchService.search(searchTerm, maxResults).stream()
                .filter(vaultItem -> !vaultItem.getMediaContent().getModels().isEmpty())
                .map(vaultItem -> ItemData.builder()
                        .id(vaultItem.getId())
                        .title(new TranslatableText(vaultItem.getTitle().getValue(), vaultItem.getTitle().getTranslations()))
                        .description(new TranslatableText(vaultItem.getDescription().getValue(), vaultItem.getDescription().getTranslations()))
                        .modelFile(vaultItem.getMediaContent().getModels().get(0))
                        .build())
                .toList();
    }

}
