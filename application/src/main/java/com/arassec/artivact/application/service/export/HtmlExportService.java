package com.arassec.artivact.application.service.export;

import com.arassec.artivact.application.port.in.export.ExportHtmlUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.widget.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PAGE_EXCHANGE_FILE_SUFFIX;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.SEARCH_RESULT_FILE_SUFFIX;

/**
 * Service for generating static HTML pages during export.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HtmlExportService implements ExportHtmlUseCase {

    /**
     * CSS resource path.
     */
    private static final String CSS_RESOURCE = "templates/export/artivact-export.css";

    /**
     * CSS filename in export.
     */
    private static final String CSS_FILENAME = "artivact-export.css";

    /**
     * The file repository.
     */
    private final FileRepository fileRepository;

    /**
     * The Thymeleaf template engine.
     */
    private final TemplateEngine templateEngine;

    /**
     * The JSON mapper.
     */
    private final JsonMapper jsonMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportHtml(ExportContext exportContext, CollectionExport collectionExport, Menu menu, List<String> availableLocales) {
        List<String> locales = availableLocales != null && !availableLocales.isEmpty()
                ? availableLocales
                : List.of("");

        copyCssFile(exportContext);

        List<NavigationItem> navigationItems = buildNavigationItems(menu);

        for (String locale : locales) {
            String localePrefix = locale.isEmpty() ? "" : locale + "/";

            if (!locale.isEmpty()) {
                fileRepository.createDirIfRequired(exportContext.getExportDir().resolve(locale));
            }

            generateHomePage(exportContext, collectionExport, navigationItems, locales, locale, localePrefix);
            generateMenuPages(exportContext, menu, navigationItems, locales, locale, localePrefix);
        }
    }

    /**
     * Copies the CSS file to the export directory.
     */
    private void copyCssFile(ExportContext exportContext) {
        try {
            ClassPathResource cssResource = new ClassPathResource(CSS_RESOURCE);
            try (InputStream is = cssResource.getInputStream()) {
                byte[] cssContent = is.readAllBytes();
                fileRepository.write(exportContext.getExportDir().resolve(CSS_FILENAME), cssContent);
            }
        } catch (IOException e) {
            log.warn("Could not copy CSS file to export directory.", e);
        }
    }

    /**
     * Builds the navigation items from the menu structure.
     */
    private List<NavigationItem> buildNavigationItems(Menu menu) {
        List<NavigationItem> items = new ArrayList<>();

        items.add(new NavigationItem("Home", "Home", "index.html", List.of()));

        if (menu.getMenuEntries() != null) {
            for (Menu entry : menu.getMenuEntries()) {
                if (entry.isHidden()) {
                    continue;
                }
                items.add(buildNavigationItem(entry));
            }
        }

        return items;
    }

    /**
     * Builds a single navigation item from a menu entry.
     */
    private NavigationItem buildNavigationItem(Menu menuEntry) {
        String label = resolveTranslatableValue(menuEntry, "");
        String translatedLabel = label;

        List<NavigationItem> children = new ArrayList<>();
        if (menuEntry.getMenuEntries() != null) {
            for (Menu child : menuEntry.getMenuEntries()) {
                if (child.isHidden()) {
                    continue;
                }
                children.add(buildNavigationItem(child));
            }
        }

        String filename = menuEntry.getTargetPageId() != null
                ? menuEntry.getTargetPageId() + ".html"
                : (children.isEmpty() ? "#" : children.getFirst().filename());

        return new NavigationItem(label, translatedLabel, filename, children);
    }

    /**
     * Resolves the value from a BaseTranslatableRestrictedObject (Menu extends TranslatableString).
     */
    private String resolveTranslatableValue(Menu menu, String locale) {
        if (menu.getTranslations() != null && !locale.isEmpty() && menu.getTranslations().containsKey(locale)) {
            return menu.getTranslations().get(locale);
        }
        return menu.getValue() != null ? menu.getValue() : "";
    }

    /**
     * Generates the home/overview page.
     */
    private void generateHomePage(ExportContext exportContext, CollectionExport collectionExport,
                                  List<NavigationItem> navigationItems, List<String> locales,
                                  String locale, String localePrefix) {
        Context context = createBaseContext(navigationItems, locales, locale, "index.html");
        context.setVariable("pageTitle", resolveTranslatable(collectionExport.getTitle(), locale));
        context.setVariable("description", resolveTranslatable(collectionExport.getDescription(), locale));
        context.setVariable("content", resolveTranslatable(collectionExport.getContent(), locale));
        context.setVariable("coverPictureExtension", collectionExport.getCoverPictureExtension());

        String html = templateEngine.process("export/home", context);
        Path targetFile = exportContext.getExportDir().resolve(localePrefix + "index.html");
        fileRepository.write(targetFile, html.getBytes());
    }

    /**
     * Generates HTML pages for all menu entries recursively.
     */
    private void generateMenuPages(ExportContext exportContext, Menu menu,
                                   List<NavigationItem> navigationItems, List<String> locales,
                                   String locale, String localePrefix) {
        if (menu.getMenuEntries() == null) {
            return;
        }

        for (Menu entry : menu.getMenuEntries()) {
            generatePageForMenu(exportContext, entry, navigationItems, locales, locale, localePrefix);
        }
    }

    /**
     * Generates a page for a single menu entry and recurses into children.
     */
    private void generatePageForMenu(ExportContext exportContext, Menu menuEntry,
                                     List<NavigationItem> navigationItems, List<String> locales,
                                     String locale, String localePrefix) {
        if (menuEntry.getTargetPageId() != null) {
            Path pageContentFile = exportContext.getExportDir().resolve(menuEntry.getTargetPageId() + PAGE_EXCHANGE_FILE_SUFFIX);
            if (fileRepository.exists(pageContentFile)) {
                PageContent pageContent = jsonMapper.readValue(fileRepository.read(pageContentFile), PageContent.class);
                generatePageHtml(exportContext, menuEntry.getTargetPageId(), pageContent,
                        navigationItems, locales, locale, localePrefix);
            }
        }

        if (menuEntry.getMenuEntries() != null) {
            for (Menu child : menuEntry.getMenuEntries()) {
                generatePageForMenu(exportContext, child, navigationItems, locales, locale, localePrefix);
            }
        }
    }

    /**
     * Generates an HTML page from page content.
     */
    private void generatePageHtml(ExportContext exportContext, String pageId, PageContent pageContent,
                                  List<NavigationItem> navigationItems, List<String> locales,
                                  String locale, String localePrefix) {
        Context context = createBaseContext(navigationItems, locales, locale, pageId + ".html");

        List<Map<String, Object>> widgetDataList = new ArrayList<>();
        for (Widget widget : pageContent.getWidgets()) {
            Map<String, Object> widgetData = new HashMap<>();
            widgetData.put("type", widget.getType().name());
            widgetData.put("id", widget.getId());

            switch (widget) {
                case PageTitleWidget pageTitleWidget -> {
                    widgetData.put("title", resolveTranslatable(pageTitleWidget.getTitle(), locale));
                    widgetData.put("subtitle", resolveTranslatable(pageTitleWidget.getSubtitle(), locale));
                    widgetData.put("backgroundImage", pageTitleWidget.getBackgroundImage());
                }
                case TextWidget textWidget -> {
                    widgetData.put("heading", resolveTranslatable(textWidget.getHeading(), locale));
                    widgetData.put("content", resolveTranslatable(textWidget.getContent(), locale));
                }
                case AvatarWidget avatarWidget -> {
                    widgetData.put("avatarImage", avatarWidget.getAvatarImage());
                    widgetData.put("avatarSubtext", resolveTranslatable(avatarWidget.getAvatarSubtext(), locale));
                }
                case InfoBoxWidget infoBoxWidget -> {
                    widgetData.put("heading", resolveTranslatable(infoBoxWidget.getHeading(), locale));
                    widgetData.put("content", resolveTranslatable(infoBoxWidget.getContent(), locale));
                    widgetData.put("boxType", infoBoxWidget.getBoxType());
                }
                case ItemSearchWidget itemSearchWidget -> {
                    widgetData.put("heading", resolveTranslatable(itemSearchWidget.getHeading(), locale));
                    widgetData.put("content", resolveTranslatable(itemSearchWidget.getContent(), locale));
                    widgetData.put("items", loadSearchResultItems(exportContext, itemSearchWidget, locale));
                }
                case ImageGalleryWidget imageGalleryWidget -> {
                    widgetData.put("heading", resolveTranslatable(imageGalleryWidget.getHeading(), locale));
                    widgetData.put("content", resolveTranslatable(imageGalleryWidget.getContent(), locale));
                    widgetData.put("images", imageGalleryWidget.getImages() != null ? imageGalleryWidget.getImages() : List.of());
                    widgetData.put("iconMode", imageGalleryWidget.isIconMode());
                    widgetData.put("hideBorder", imageGalleryWidget.isHideBorder());
                    widgetData.put("stretchImages", imageGalleryWidget.isStretchImages());
                }
                default -> log.debug("Unsupported widget type for HTML export: {}", widget.getType());
            }

            widgetDataList.add(widgetData);
        }

        context.setVariable("widgets", widgetDataList);
        context.setVariable("pageId", pageId);

        String html = templateEngine.process("export/page", context);
        Path targetFile = exportContext.getExportDir().resolve(localePrefix + pageId + ".html");
        fileRepository.write(targetFile, html.getBytes());
    }

    /**
     * Loads search result items for an ItemSearchWidget.
     */
    private List<Map<String, String>> loadSearchResultItems(ExportContext exportContext,
                                                            ItemSearchWidget widget,
                                                            String locale) {
        List<Map<String, String>> items = new ArrayList<>();
        Path searchResultFile = exportContext.getExportDir().resolve(widget.getId() + SEARCH_RESULT_FILE_SUFFIX);
        if (!fileRepository.exists(searchResultFile)) {
            return items;
        }

        String[] itemIds = jsonMapper.readValue(fileRepository.read(searchResultFile), String[].class);
        for (String itemId : itemIds) {
            Path itemJsonFile = exportContext.getExportDir().resolve(itemId).resolve("artivact.item.json");
            if (fileRepository.exists(itemJsonFile)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> itemData = jsonMapper.readValue(fileRepository.read(itemJsonFile), Map.class);
                Map<String, String> itemInfo = new HashMap<>();
                itemInfo.put("id", itemId);
                itemInfo.put("title", resolveTranslatableMap(itemData.get("title"), locale));
                String imageFile = resolveFirstImage(itemData);
                if (imageFile != null) {
                    itemInfo.put("image", itemId + "/" + imageFile);
                }
                items.add(itemInfo);
            }
        }

        return items;
    }

    /**
     * Resolves the first image from item data.
     */
    @SuppressWarnings("unchecked")
    private String resolveFirstImage(Map<String, Object> itemData) {
        Object mediaContent = itemData.get("mediaContent");
        if (mediaContent instanceof Map<?, ?> mc) {
            Object images = mc.get("images");
            if (images instanceof List<?> imageList && !imageList.isEmpty()) {
                return imageList.getFirst().toString();
            }
        }
        return null;
    }

    /**
     * Resolves a translatable value from a raw map structure.
     */
    @SuppressWarnings("unchecked")
    private String resolveTranslatableMap(Object translatableObj, String locale) {
        if (translatableObj instanceof Map<?, ?> map) {
            if (!locale.isEmpty()) {
                Object translations = map.get("translations");
                if (translations instanceof Map<?, ?> translationsMap && translationsMap.containsKey(locale)) {
                    return translationsMap.get(locale).toString();
                }
            }
            Object value = map.get("value");
            return value != null ? value.toString() : "";
        }
        return "";
    }

    /**
     * Creates the base Thymeleaf context with navigation and locale data.
     */
    private Context createBaseContext(List<NavigationItem> navigationItems, List<String> locales,
                                     String locale, String currentPageFilename) {
        Context context = new Context();
        context.setVariable("navigationItems", resolveNavigationLabels(navigationItems, locale));
        context.setVariable("locales", locales);
        context.setVariable("currentLocale", locale);
        context.setVariable("currentPageFilename", currentPageFilename);

        String cssPath = locale.isEmpty() ? CSS_FILENAME : "../" + CSS_FILENAME;
        context.setVariable("cssPath", cssPath);

        String localeBasePath = locale.isEmpty() ? "" : "../";
        context.setVariable("localeBasePath", localeBasePath);

        return context;
    }

    /**
     * Resolves navigation labels for a specific locale.
     */
    private List<NavigationItem> resolveNavigationLabels(List<NavigationItem> items, String locale) {
        List<NavigationItem> resolved = new ArrayList<>();
        for (NavigationItem item : items) {
            String label = "Home".equals(item.label()) ? "Home" : item.label();
            resolved.add(new NavigationItem(
                    label,
                    item.translatedLabel(),
                    item.filename(),
                    resolveNavigationLabels(item.children(), locale)
            ));
        }
        return resolved;
    }

    /**
     * Resolves a translatable string for a specific locale.
     */
    private String resolveTranslatable(TranslatableString translatableString, String locale) {
        if (translatableString == null) {
            return "";
        }
        if (!locale.isEmpty() && translatableString.getTranslations() != null
                && translatableString.getTranslations().containsKey(locale)) {
            return translatableString.getTranslations().get(locale);
        }
        return translatableString.getValue() != null ? translatableString.getValue() : "";
    }

    /**
     * A navigation item for the menu bar.
     *
     * @param label           The default label.
     * @param translatedLabel The translated label.
     * @param filename        The target HTML filename.
     * @param children        Child navigation items.
     */
    public record NavigationItem(String label, String translatedLabel, String filename,
                                 List<NavigationItem> children) {
    }

}
