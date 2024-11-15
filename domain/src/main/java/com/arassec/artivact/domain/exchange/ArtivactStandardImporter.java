package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.item.MediaCreationContent;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ExportMainData;
import com.arassec.artivact.domain.exchange.model.ExportSourceType;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Standard {@link ArtivactImporter}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArtivactStandardImporter implements ArtivactImporter, ExchangeProcessor {

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
     * Service for item handling.
     */
    private final ItemService itemService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void importItem(Path itemExport) {
        Path itemExportDir = projectDataProvider.getProjectRoot()
                .resolve(ProjectDataProvider.TEMP_DIR)
                .resolve(itemExport.getFileName().toString().replace(ZIP_FILE_SUFFIX, ""));

        fileRepository.unpack(itemExport, itemExportDir);

        try {
            ExportMainData exportMainData = objectMapper.readValue(itemExportDir.resolve(CONTENT_EXPORT_FILENAME_JSON).toFile(), ExportMainData.class);

            if (!ExportSourceType.ITEM.equals(exportMainData.getExportSourceType())) {
                throw new ArtivactException("Invalid export source type for item import: " + exportMainData.getExportSourceType());
            }

            String itemId = exportMainData.getExportSourceId();

            Path itemDir = itemExportDir.resolve(itemId);
            String itemJson = Files.readString(itemDir.resolve(ITEM_EXPORT_FILENAME_JSON));

            Item item = objectMapper.readValue(itemJson, Item.class);
            item.setMediaCreationContent(new MediaCreationContent());

            item.getMediaContent().getImages().forEach(image -> {
                try {
                    itemService.saveImage(item.getId(), image, Files.newInputStream(itemDir.resolve(image)), true);
                } catch (IOException e) {
                    throw new ArtivactException("Could not read image from imported ZIP file!", e);
                }
            });

            item.getMediaContent().getModels().forEach(model -> {
                try {
                    itemService.saveModel(item.getId(), model, Files.newInputStream(itemDir.resolve(model)), true);
                } catch (IOException e) {
                    throw new ArtivactException("Could not read model from imported ZIP file!", e);
                }
            });

            itemService.save(item);

            fileRepository.delete(itemExportDir);

        } catch (IOException e) {
            throw new ArtivactException("Could not import item!", e);
        }
    }

}
