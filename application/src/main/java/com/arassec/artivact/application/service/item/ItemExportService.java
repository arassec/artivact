package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.ExportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ExportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.ExportItemUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.service.BaseExportService;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
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

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Service for item export.
 */
public class ItemExportService extends BaseExportService implements ExportItemUseCase {

    @Getter
    /**
     * Use case for use project dirs.
     */
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    @Getter
    /**
     * The json mapper.
     */
    private final JsonMapper jsonMapper;

    @Getter
    /**
     * Repository for file.
     */
    private final FileRepository fileRepository;

    /**
     * Use case for load item.
     */
    private final LoadItemUseCase loadItemUseCase;

    /**
     * Use case for export properties configuration.
     */
    private final ExportPropertiesConfigurationUseCase exportPropertiesConfigurationUseCase;

    /**
     * Use case for export tags configuration.
     */
    private final ExportTagsConfigurationUseCase exportTagsConfigurationUseCase;

    /**
     * Use case for load properties configuration.
     */
    private final LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;

    /**
     * Use case for load tags configuration.
     */
    private final LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportItem(String itemId) {
        Item item = loadItemUseCase.loadTranslated(itemId);
        return exportItem(item, loadPropertiesConfigurationUseCase.loadPropertiesConfiguration(), loadTagsConfigurationUseCase.loadTagsConfiguration());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportItem(ExportContext exportContext, Item item) {
        Path itemExportDir = exportContext.getExportDir().resolve(item.getId());

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
     * {@inheritDoc}
     */
    @Override
    public Path exportItem(Item item, PropertiesConfiguration propertiesConfiguration, TagsConfiguration tagsConfiguration) {
        ExportContext exportContext = createExportContext(item.getId(), null);

        prepareExport(exportContext);

        exportMainData(exportContext, ContentSource.ITEM, item.getId(), item.getTitle(), item.getDescription(), null);

        exportPropertiesConfigurationUseCase.exportPropertiesConfiguration(exportContext, propertiesConfiguration);
        exportTagsConfigurationUseCase.exportTagsConfiguration(exportContext, tagsConfiguration);

        exportItem(exportContext, item);

        fileRepository.pack(exportContext.getExportDir(), exportContext.getExportFile());
        fileRepository.delete(exportContext.getExportDir());

        return exportContext.getExportFile();
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

        item.setMediaCreationContent(null); // Not needed in standard exports at the moment.

        Path imagesSourceDir = fileRepository.getDirFromId(useProjectDirsUseCase.getItemsDir(), item.getId())
                .resolve(DirectoryDefinitions.IMAGES_DIR);
        Path modelsSourceDir = fileRepository.getDirFromId(useProjectDirsUseCase.getItemsDir(), item.getId())
                .resolve(DirectoryDefinitions.MODELS_DIR);

        if (exportContext.getExportConfiguration().isOptimizeSize()) {
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
            item.getMediaContent().getModels()
                    .forEach(model -> fileRepository.copy(modelsSourceDir.resolve(model), itemExportDir.resolve(model)));
            item.getMediaContent().getImages()
                    .forEach(image -> fileRepository.copy(imagesSourceDir.resolve(image), itemExportDir.resolve(image)));
        }
    }

}
