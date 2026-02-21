package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.item.ExportItemUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.service.BaseExportService;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.ITEM_EXCHANGE_FILENAME_JSON;

/**
 * Service for item export.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemExportService extends BaseExportService implements ExportItemUseCase {

    /**
     * Use case for use project dirs.
     */
    @Getter
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * The JSON mapper.
     */
    @Getter
    private final JsonMapper jsonMapper;

    /**
     * Repository for file.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * Use case for load item.
     */
    private final LoadItemUseCase loadItemUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportItem(String itemId) {
        Item item = loadItemUseCase.loadTranslated(itemId);
        return exportItem(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportItem(Item item) {
        ExportContext exportContext = createExportContext(item.getId(), null);

        prepareExport(exportContext);

        exportMainData(exportContext, ContentSource.ITEM, item.getId(), item.getTitle(), item.getDescription(), null);
        exportConfigs(exportContext);
        exportItem(exportContext, item);

        cleanupExport(exportContext);

        return exportContext.getExportFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportItem(ExportContext exportContext, Item item) {
        Path itemExportDir = fileRepository.getDirFromId(exportContext.getExportDir().resolve(DirectoryDefinitions.ITEMS_DIR), item.getId());

        // Already exported by another widget / exporter? Skip item...
        if (fileRepository.exists(itemExportDir)) {
            return;
        } else {
            fileRepository.createDirIfRequired(itemExportDir);
        }

        copyItemMediaFiles(exportContext, item, itemExportDir);

        item.getTitle().setTranslatedValue(null);
        item.getDescription().setTranslatedValue(null);

        writeJsonFile(itemExportDir.resolve(ITEM_EXCHANGE_FILENAME_JSON), item);
    }

    /**
     * Copies a created export to an output stream and deletes it afterward.
     *
     * @param export       The export.
     * @param outputStream The target stream.
     */
    @Override
    public void copyExportAndDelete(Path export, OutputStream outputStream) {
        fileRepository.copy(export, outputStream);
        fileRepository.delete(export);
    }

    /**
     * Copies the item's media files to the target directory.
     *
     * @param exportContext Export context.
     * @param item          The item to export.
     * @param itemExportDir The item's export directory.
     */
    private void copyItemMediaFiles(ExportContext exportContext, Item item, Path itemExportDir) {
        List<String> images = item.getMediaContent().getImages();
        List<String> models = item.getMediaContent().getModels();

        Path imagesSourceDir = fileRepository.getDirFromId(useProjectDirsUseCase.getItemsDir(), item.getId())
                .resolve(DirectoryDefinitions.IMAGES_DIR);
        Path modelsSourceDir = fileRepository.getDirFromId(useProjectDirsUseCase.getItemsDir(), item.getId())
                .resolve(DirectoryDefinitions.MODELS_DIR);

        Path imagesTargetDir = itemExportDir.resolve(DirectoryDefinitions.IMAGES_DIR);
        fileRepository.createDirIfRequired(imagesTargetDir);
        Path modelsTargetDir = itemExportDir.resolve(DirectoryDefinitions.MODELS_DIR);
        fileRepository.createDirIfRequired(modelsTargetDir);

        if (exportContext.getExportConfiguration().isXrExport()) {
            item.setMediaCreationContent(null); // Not needed in XR exports!
            if (!models.isEmpty()) {
                String firstModel = models.getFirst();
                fileRepository.copy(modelsSourceDir.resolve(firstModel), itemExportDir.resolve(firstModel), StandardCopyOption.REPLACE_EXISTING);
                item.getMediaContent().getImages().clear();
                item.getMediaContent().getModels().retainAll(List.of(firstModel));
            } else if (!images.isEmpty()) {
                String firstImage = images.getFirst();
                fileRepository.copy(imagesSourceDir.resolve(firstImage), itemExportDir.resolve(firstImage), StandardCopyOption.REPLACE_EXISTING);
                item.getMediaContent().getModels().clear();
                item.getMediaContent().getImages().retainAll(List.of(firstImage));
            }
        } else {
            item.getMediaContent().getImages()
                    .forEach(image -> fileRepository.copy(imagesSourceDir.resolve(image), imagesTargetDir.resolve(image)));
            item.getMediaContent().getModels()
                    .forEach(model -> fileRepository.copy(modelsSourceDir.resolve(model), modelsTargetDir.resolve(model)));
            // TODO: Add option to include media creation files when exporting an item.
            item.setMediaCreationContent(null);
            /*
            item.getMediaCreationContent().getImageSets()
                    .forEach(imageSet -> imageSet.getFiles()
                            .forEach(image -> fileRepository.copy(imagesSourceDir.resolve(image), imagesTargetDir.resolve(image))));
            item.getMediaCreationContent().getModelSets()
                    .forEach(modelSet -> fileRepository.copy(
                            useProjectDirsUseCase.getProjectRoot().resolve(modelSet.getDirectory()),
                            exportContext.getExportDir().resolve(modelSet.getDirectory())
                    ));
             */
        }
    }

}
