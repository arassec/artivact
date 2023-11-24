package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.PageEntityRepository;
import com.arassec.artivact.backend.persistence.model.PageEntity;
import com.arassec.artivact.backend.service.aop.GenerateIds;
import com.arassec.artivact.backend.service.aop.RestrictResult;
import com.arassec.artivact.backend.service.aop.TranslateResult;
import com.arassec.artivact.backend.service.model.BaseRestrictedItem;
import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.page.FileProcessingWidget;
import com.arassec.artivact.backend.service.model.page.Page;
import com.arassec.artivact.backend.service.model.page.PageContent;
import com.arassec.artivact.backend.service.model.page.widget.TextWidget;
import com.arassec.artivact.backend.service.model.item.ImageSize;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PageService extends BaseFileService {

    public static final String WIDGETS_FILE_DIR = "widgets";

    private final PageEntityRepository pageEntityRepository;

    @Getter
    private final ObjectMapper objectMapper;

    private final Path widgetFilesDir;

    public PageService(PageEntityRepository pageEntityRepository,
                       ObjectMapper objectMapper,
                       ProjectRootProvider projectRootProvider) {
        this.pageEntityRepository = pageEntityRepository;
        this.objectMapper = objectMapper;
        this.widgetFilesDir = projectRootProvider.getProjectRoot().resolve(WIDGETS_FILE_DIR);
    }

    public Page createPage(Set<String> restrictions) {
        PageContent pageContent = new PageContent();
        pageContent.setId(UUID.randomUUID().toString());
        pageContent.setRestrictions(restrictions);

        Page page = new Page();
        page.setId(UUID.randomUUID().toString());
        page.setVersion(0);
        page.setPageContent(pageContent);

        PageEntity pageEntity = new PageEntity();
        pageEntity.setId(page.getId());
        pageEntity.setVersion(page.getVersion());
        pageEntity.setContentJson(toJson(pageContent));

        pageEntityRepository.save(pageEntity);

        return page;
    }

    public void deletePage(String pageId) {
        Optional<PageEntity> pageEntityOptional = pageEntityRepository.findById(pageId);
        if (pageEntityOptional.isPresent()) {
            PageEntity pageEntity = pageEntityOptional.get();
            PageContent pageContent = fromJson(pageEntity.getContentJson(), PageContent.class);
            pageContent.getWidgets().forEach(widget -> deleteDirAndEmptyParents(getDirFromId(widgetFilesDir, widget.getId())));
            pageEntityRepository.deleteById(pageId);
        }
    }

    public void updatePageRestrictions(String pageId, Set<String> restrictions) {
        PageEntity pageEntity = pageEntityRepository.findById(pageId).orElseThrow();

        PageContent pageContent = fromJson(pageEntity.getContentJson(), PageContent.class);
        pageContent.setRestrictions(restrictions);

        pageEntity.setContentJson(toJson(pageContent));
        pageEntityRepository.save(pageEntity);
    }

    @TranslateResult
    @RestrictResult
    public PageContent loadIndexPageContent() {
        Optional<PageEntity> indexPageOptional = pageEntityRepository.findFirstByIndexPage(true);
        if (indexPageOptional.isPresent()) {
            return processPageEntity(indexPageOptional.get());
        } else {
            TranslatableString fallbackText = new TranslatableString();
            fallbackText.setValue("No index page has been defined yet. "
                    + "Create a menu, add a page to it and edit it to be the index page.");
            TextWidget fallbackTextWidget = new TextWidget();
            fallbackTextWidget.setContent(fallbackText);
            PageContent fallbackIndexPage = new PageContent();
            fallbackIndexPage.getWidgets().add(fallbackTextWidget);
            return fallbackIndexPage;
        }
    }

    @TranslateResult
    @RestrictResult
    public PageContent loadPageContent(String pageId) {
        return processPageEntity(pageEntityRepository.findById(pageId).orElseThrow());
    }

    @GenerateIds
    @TranslateResult
    @RestrictResult
    public PageContent savePageContent(String pageId, PageContent pageContent) {
        PageEntity pageEntity = pageEntityRepository.findById(pageId).orElseThrow();

        PageContent existingPageContent = fromJson(pageEntity.getContentJson(), PageContent.class);
        List<String> widgetIdsToDelete = existingPageContent.getWidgets().stream()
                .map(BaseRestrictedItem::getId)
                .collect(Collectors.toList());

        widgetIdsToDelete.removeAll(pageContent.getWidgets().stream()
                .map(BaseRestrictedItem::getId)
                .toList());

        widgetIdsToDelete.forEach(widgetId -> deleteDirAndEmptyParents(getDirFromId(widgetFilesDir, widgetId)));

        pageEntity.setIndexPage(pageContent.isIndexPage());
        pageEntity.setContentJson(toJson(pageContent));
        pageEntityRepository.save(pageEntity);
        return pageContent;
    }


    public String saveFile(String pageId, String widgetId, MultipartFile file) {

        String fileName = saveFile(widgetFilesDir, widgetId, file);

        PageEntity pageEntity = pageEntityRepository.findById(pageId).orElseThrow();
        PageContent pageContent = fromJson(pageEntity.getContentJson(), PageContent.class);
        pageContent.getWidgets().forEach(widget -> {
            if (widget.getId().equals(widgetId) && widget instanceof FileProcessingWidget fileProcessingWidget) {
                fileProcessingWidget.processFile(fileName);
            }
        });

        pageEntity.setContentJson(toJson(pageContent));
        pageEntityRepository.save(pageEntity);

        return fileName;
    }

    public FileSystemResource loadFile(String widgetId, String fileName, ImageSize targetSize) {
        return loadFile(widgetFilesDir, widgetId, fileName, targetSize);
    }

    private PageContent processPageEntity(PageEntity pageEntity) {
        PageContent pageContent = fromJson(pageEntity.getContentJson(), PageContent.class);

        PageContent result = new PageContent();
        result.setId(pageContent.getId());
        result.setIndexPage(pageContent.isIndexPage());
        result.setWidgets(pageContent.getWidgets());

        return result;
    }

}
