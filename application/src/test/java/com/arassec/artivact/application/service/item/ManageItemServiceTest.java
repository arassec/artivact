package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.in.search.ManageSearchIndexUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.ItemRepository;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaContent;
import com.arassec.artivact.domain.model.item.MediaCreationContent;
import com.arassec.artivact.domain.model.property.Property;
import com.arassec.artivact.domain.model.property.PropertyCategory;
import com.arassec.artivact.domain.model.tag.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @InjectMocks
    private ManageItemService service;

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

    @Test
    void testLoadReturnsEmptyWhenNotFound() {
        when(itemRepository.findById("id")).thenReturn(Optional.empty());
        Optional<Item> result = service.load("id");
        assertThat(result).isEmpty();
    }

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

    @Test
    void testLoadTranslatedThrowsIfNotFound() {
        when(itemRepository.findById("id")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadTranslated("id"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void testSaveDeletesDanglingFilesAndUpdatesIndex() {
        Item item = new Item();
        item.setId("id1");
        item.setMediaContent(new MediaContent());
        item.setMediaCreationContent(new MediaCreationContent());

        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));
        when(fileRepository.listNamesWithoutScaledImages(any())).thenReturn(new ArrayList<>());
        when(fileRepository.getDirFromId(any(), any())).thenReturn(Path.of("items/id1"));
        when(itemRepository.save(item)).thenReturn(item);

        Item result = service.save(item);

        assertThat(result).isSameAs(item);
        verify(manageSearchIndexUseCase).updateIndex(item);
        verify(itemRepository).save(item);
    }

    @Test
    void testDeleteRemovesFromRepositoryAndFilesystem() {
        Path itemsPath = Path.of("items");
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(itemsPath);
        Path itemDirPath = Path.of("items/id");
        when(fileRepository.getDirFromId(itemsPath, "id")).thenReturn(itemDirPath);

        service.delete("id");

        verify(itemRepository).deleteById("id");
        verify(fileRepository).deleteDirAndEmptyParents(itemDirPath);
    }

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
