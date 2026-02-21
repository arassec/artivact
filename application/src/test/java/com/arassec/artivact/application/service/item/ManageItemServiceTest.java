package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.in.search.ManageSearchIndexUseCase;
import com.arassec.artivact.application.port.out.repository.FavoriteRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.ItemRepository;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaContent;
import com.arassec.artivact.domain.model.item.MediaCreationContent;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.property.Property;
import com.arassec.artivact.domain.model.property.PropertyCategory;
import com.arassec.artivact.domain.model.tag.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ManageSearchIndexUseCase manageSearchIndexUseCase;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;

    @Mock
    private LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private ManageItemService service;

    /**
     * Tests that creating a new item initializes it with default values and tags.
     */
    @Test
    void testCreateInitializesItemWithDefaults() {
        Tag defaultTag = Tag.builder().id("t1").defaultTag(true).build();
        when(loadTagsConfigurationUseCase.loadTranslatedRestrictedTagsConfiguration())
                .thenReturn(new TagsConfiguration(List.of(defaultTag)));

        Item item = service.create();

        assertThat(item.getId()).isNull();
        assertThat(item.getTags()).containsExactly(defaultTag);
        assertThat(item.getVersion()).isZero();
        assertThat(item.getMediaContent()).isNotNull();
        assertThat(item.getMediaCreationContent()).isNotNull();
        assertThat(item.getRestrictions()).isNotEmpty();
    }

    /**
     * Tests that loading an item returns empty if the item does not exist.
     */
    @Test
    void testLoadReturnsEmptyWhenNotFound() {
        when(itemRepository.findById("id")).thenReturn(Optional.empty());
        Optional<Item> result = service.load("id");
        assertThat(result).isEmpty();
    }

    /**
     * Tests that loading an item updates its tags and adds missing properties based on configuration.
     */
    @Test
    void testLoadUpdatesTagsAndAddsMissingProperties() {
        Tag t1 = Tag.builder().id("1").build();
        Item item = new Item();
        item.setTags(new ArrayList<>(List.of(t1)));
        item.setProperties(new HashMap<>());
        item.setMediaContent(new MediaContent());
        item.setMediaCreationContent(new MediaCreationContent());

        Tag updated = Tag.builder().id("1").defaultTag(true).build();
        when(itemRepository.findById("id")).thenReturn(Optional.of(item));
        when(loadTagsConfigurationUseCase.loadTagsConfiguration()).thenReturn(new TagsConfiguration(List.of(updated)));

        Property property = new Property();
        property.setId("p1");
        PropertyCategory category = new PropertyCategory();
        category.setProperties(List.of(property));
        PropertiesConfiguration propsConfig = new PropertiesConfiguration(List.of(category));
        when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()).thenReturn(propsConfig);

        Optional<Item> result = service.load("id");

        assertThat(result).isPresent();
        assertThat(result.get().getTags()).containsExactly(updated);
        assertThat(result.get().getProperties()).containsKey("p1");
    }

    /**
     * Tests that loading a translated item throws an exception if the item is not found.
     */
    @Test
    void testLoadTranslatedThrowsIfNotFound() {
        when(itemRepository.findById("id")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadTranslated("id"))
                .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Tests that loading a translated and restricted item throws an exception if the item is not found.
     */
    @Test
    void testLoadTranslatedRestrictedThrowsIfNotFound() {
        when(itemRepository.findById("id")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadTranslatedRestricted("id"))
                .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Tests that saving an item deletes dangling files (images and models) and updates the search index.
     */
    @Test
    void testSaveDeletesDanglingFilesAndUpdatesIndex() {
        Item item = new Item();
        item.setId("id1");
        item.setMediaContent(new MediaContent());

        List<String> images = new LinkedList<>();
        images.add("img1.jpg");
        images.add("img3.jpg");
        item.getMediaContent().setImages(images);

        List<String> models = new LinkedList<>();
        models.add("model1.glb");
        models.add("model3.glb");
        item.getMediaContent().setModels(models);

        item.setMediaCreationContent(new MediaCreationContent());

        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));

        // Mock images: img1.jpg is in item, img2.jpg is dangling, img3.jpg is missing in the filesystem
        when(fileRepository.getDirFromId(any(), any())).thenReturn(Path.of("items/id1"));
        when(fileRepository.listNamesWithoutScaledImages(Path.of("items/id1/images"))).thenReturn(new ArrayList<>(List.of("img1.jpg", "img2.jpg")));
        lenient().when(fileRepository.exists(Path.of("items/id1/images/img1.jpg"))).thenReturn(true);
        when(fileRepository.getSubdirFilePath(any(), any(), eq(DirectoryDefinitions.IMAGES_DIR))).thenReturn(Path.of("items/id1/images"));

        // Mock models: model1.glb is in item, model2.glb is dangling, model3.glb is missing in the filesystem
        when(fileRepository.listNamesWithoutScaledImages(Path.of("items/id1/models"))).thenReturn(new ArrayList<>(List.of("model1.glb", "model2.glb")));
        lenient().when(fileRepository.exists(Path.of("items/id1/models/model1.glb"))).thenReturn(true);
        when(fileRepository.getSubdirFilePath(any(), any(), eq(DirectoryDefinitions.MODELS_DIR))).thenReturn(Path.of("items/id1/models"));

        when(itemRepository.save(item)).thenReturn(item);

        Item result = service.save(item);

        assertThat(result).isSameAs(item);

        // Verify missing files are removed from item
        ArgumentCaptor<Item> argCap = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(argCap.capture());
        assertThat(argCap.getValue().getMediaContent().getImages()).hasSize(1).containsExactly("img1.jpg");
        assertThat(argCap.getValue().getMediaContent().getModels()).hasSize(1).containsExactly("model1.glb");

        // Verify dangling image deletion (original + scaled versions)
        verify(fileRepository).delete(Path.of("items/id1/images/img2.jpg"));
        verify(fileRepository, atLeastOnce()).delete(argThat(path -> path.toString().contains("img2.jpg") && path.toString().contains("-"))); // Scaled versions

        // Verify dangling model deletion
        verify(fileRepository).delete(Path.of("items/id1/models/model2.glb"));

        verify(manageSearchIndexUseCase).updateIndex(item);
        verify(itemRepository).save(item);
    }

    /**
     * Tests that deleting an item removes it from the repository and filesystem.
     */
    @Test
    void testDeleteRemovesFromRepositoryAndFilesystem() {
        Path itemsPath = Path.of("items");
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(itemsPath);
        Path itemDirPath = Path.of("items/id");
        when(fileRepository.getDirFromId(itemsPath, "id")).thenReturn(itemDirPath);

        service.delete("id");

        verify(favoriteRepository).deleteByItemId("id");
        verify(itemRepository).deleteById("id");
        verify(fileRepository).deleteAndPruneEmptyParents(itemDirPath);
    }

    /**
     * Tests that deleting an item handles exceptions from the favorite repository gracefully.
     */
    @Test
    void testDeleteHandlesFavoriteRepositoryException() {
        Path itemsPath = Path.of("items");
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(itemsPath);
        Path itemDirPath = Path.of("items/id");
        when(fileRepository.getDirFromId(itemsPath, "id")).thenReturn(itemDirPath);

        doThrow(new RuntimeException("DB Error")).when(favoriteRepository).deleteByItemId("id");

        service.delete("id");

        verify(favoriteRepository).deleteByItemId("id");
        verify(itemRepository).deleteById("id");
        verify(fileRepository).deleteAndPruneEmptyParents(itemDirPath);
    }

    /**
     * Tests that loading modified items returns the mapped items.
     */
    @Test
    void testLoadModifiedReturnsMappedItems() {
        Item item = new Item();
        when(itemRepository.findItemIdsForRemoteExport(5)).thenReturn(List.of("i1"));
        when(itemRepository.findById("i1")).thenReturn(Optional.of(item));

        when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()).thenReturn(new PropertiesConfiguration(List.of()));

        List<Item> result = service.loadModified(5);

        assertThat(result).containsExactly(item);
    }

}
