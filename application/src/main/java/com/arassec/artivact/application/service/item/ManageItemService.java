package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.infrastructure.aspect.GenerateIds;
import com.arassec.artivact.application.infrastructure.aspect.RestrictResult;
import com.arassec.artivact.application.infrastructure.aspect.TranslateResult;
import com.arassec.artivact.application.port.in.configuration.LoadPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.CreateItemUseCase;
import com.arassec.artivact.application.port.in.item.DeleteItemUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.in.search.ManageSearchIndexUseCase;
import com.arassec.artivact.application.port.out.repository.FavoriteRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.ItemRepository;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaContent;
import com.arassec.artivact.domain.model.item.MediaCreationContent;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.tag.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for item handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ManageItemService implements CreateItemUseCase,
        LoadItemUseCase,
        SaveItemUseCase,
        DeleteItemUseCase {

    /**
     * Repository for items.
     */
    private final ItemRepository itemRepository;

    private final ManageSearchIndexUseCase manageSearchIndexUseCase;

    /**
     * The application's {@link FileRepository}.
     */
    private final FileRepository fileRepository;

    private final UseProjectDirsUseCase useProjectDirsUseCase;

    private final LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;

    private final LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;

    private final FavoriteRepository favoriteRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Item create() {
        TagsConfiguration tagsConfiguration = loadTagsConfigurationUseCase.loadTranslatedRestrictedTagsConfiguration();

        List<Tag> defaultTags = tagsConfiguration.getTags().stream()
                .filter(Tag::isDefaultTag)
                .toList();

        Item item = new Item();
        item.setVersion(0);
        item.setRestrictions(Set.of(Roles.ROLE_USER));
        item.setTitle(new TranslatableString());
        item.setDescription(new TranslatableString());
        item.setMediaContent(new MediaContent());
        item.setMediaCreationContent(new MediaCreationContent());
        item.setTags(defaultTags);

        return item;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("java:S6204") // Collection needs to be modifiable...
    @Override
    public Optional<Item> load(String itemId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);

        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();

            // Update existing tags and remove those, that don't exist anymore.
            TagsConfiguration tagsConfiguration = loadTagsConfigurationUseCase.loadTagsConfiguration();
            item.setTags(item.getTags().stream()
                    .map(tag -> tagsConfiguration.getTags().stream()
                            .filter(configuredTag -> tag.getId().equals(configuredTag.getId()))
                            .findFirst()
                            .orElse(null)
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));

            // Create an empty TranslatableString value for every property:
            loadPropertiesConfigurationUseCase.loadPropertiesConfiguration().getCategories().forEach(propertyCategory ->
                    propertyCategory.getProperties().forEach(property -> {
                        if (!item.getProperties().containsKey(property.getId())) {
                            item.getProperties().put(property.getId(), new TranslatableString());
                        }
                    }));

            return Optional.of(item);
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @TranslateResult
    @Override
    public Item loadTranslated(String itemId) {
        return load(itemId).orElseThrow();
    }

    /**
     * {@inheritDoc}
     */
    @TranslateResult
    @RestrictResult
    @Override
    public Item loadTranslatedRestricted(String itemId) {
        return load(itemId).orElseThrow();
    }

    /**
     * {@inheritDoc}
     */
    @GenerateIds
    @Override
    public Item save(Item item) {
        String itemId = item.getId();

        Path itemsDir = useProjectDirsUseCase.getItemsDir();

        getDanglingImages(item).forEach(imageToDelete -> {
            Path originalImage = fileRepository.getSubdirFilePath(itemsDir, itemId, DirectoryDefinitions.IMAGES_DIR).resolve(imageToDelete);
            fileRepository.delete(originalImage);
            for (ImageSize imageSize : ImageSize.values()) {
                Path scaledImage = fileRepository.getSubdirFilePath(itemsDir, itemId, DirectoryDefinitions.IMAGES_DIR)
                        .resolve(imageSize.name() + "-" + imageToDelete);
                fileRepository.delete(scaledImage);
            }
        });

        List<String> modelsInItem = item.getMediaContent().getModels();

        List<String> modelsToDelete = fileRepository.listNamesWithoutScaledImages(fileRepository.getDirFromId(itemsDir, itemId).resolve(DirectoryDefinitions.MODELS_DIR));
        modelsToDelete.removeAll(modelsInItem);
        modelsToDelete.forEach(modelToDelete ->
                fileRepository.delete(fileRepository.getSubdirFilePath(itemsDir, itemId, DirectoryDefinitions.MODELS_DIR).resolve(modelToDelete))
        );

        item = itemRepository.save(item);

        manageSearchIndexUseCase.updateIndex(item);

        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String itemId) {
        favoriteRepository.deleteByItemId(itemId);
        itemRepository.deleteById(itemId);
        fileRepository.deleteDirAndEmptyParents(fileRepository.getDirFromId(useProjectDirsUseCase.getItemsDir(), itemId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Item> loadModified(int maxItems) {
        return itemRepository.findItemIdsForRemoteExport(maxItems).stream()
                .map(itemId -> load(itemId).orElseThrow())
                .toList();
    }

    /**
     * Returns a list of the item's images that are not referenced by the item itself, but only exist in the
     * filesystem.
     *
     * @param item The item.
     * @return List of unreferenced images.
     */
    private List<String> getDanglingImages(Item item) {
        List<String> imagesInItem = new LinkedList<>(item.getMediaContent().getImages());
        item.getMediaCreationContent().getImageSets().forEach(creationImageSet -> imagesInItem.addAll(creationImageSet.getFiles()));

        List<String> allImagesInFolder = fileRepository.listNamesWithoutScaledImages(
                fileRepository.getDirFromId(useProjectDirsUseCase.getItemsDir(), item.getId())
                        .resolve(DirectoryDefinitions.IMAGES_DIR)
        );

        allImagesInFolder.removeAll(imagesInItem);

        return allImagesInFolder;
    }

}

