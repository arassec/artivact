package com.arassec.artivact.domain.exchange.imp;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.item.MediaCreationContent;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.service.ItemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.ITEM_EXCHANGE_FILENAME_JSON;

/**
 * Importer for {@link Item}s.
 */
@Component
@RequiredArgsConstructor
public class ItemImporter {

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
     * Imports an item.
     *
     * @param importContext The import context.
     * @param itemId        The item's ID.
     */
    public void importItem(ImportContext importContext, String itemId) {
        Path itemDir = importContext.getImportDir().resolve(itemId);
        String itemJson = fileRepository.read(itemDir.resolve(ITEM_EXCHANGE_FILENAME_JSON));

        try {
            Item item = objectMapper.readValue(itemJson, Item.class);

            item.setMediaCreationContent(new MediaCreationContent());

            item.getMediaContent().getImages()
                    .forEach(image -> itemService.saveImage(item.getId(), image, fileRepository.readStream(itemDir.resolve(image)), true));

            item.getMediaContent().getModels()
                    .forEach(model -> itemService.saveModel(item.getId(), model, fileRepository.readStream(itemDir.resolve(model)), true));

            itemService.save(item);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not import item!", e);
        }
    }

}
