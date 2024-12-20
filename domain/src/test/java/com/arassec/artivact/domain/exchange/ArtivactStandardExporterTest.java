package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.core.model.exchange.ContentSource;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.model.page.widget.*;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ExchangeMainData;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.PageService;
import com.arassec.artivact.domain.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ArtivactStandardExporter}.
 */
@ExtendWith(MockitoExtension.class)
class ArtivactStandardExporterTest {

    /**
     * The tested exporter.
     */
    @InjectMocks
    private ArtivactStandardExporter exporter;

    /**
     * The service for page handling.
     */
    @Mock
    private PageService pageService;

    /**
     * The service for searching items.
     */
    @Mock
    private SearchService searchService;

    /**
     * The application's file repository.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * The application's project data provider.
     */
    @Mock
    private ProjectDataProvider projectDataProvider;

    /**
     * The application's object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Path to the export directory used during tests.
     */
    private Path exportDir;

    /**
     * Path to the export file used during tests.
     */
    private Path exportFile;

    /**
     * Properties configuration for export testing.
     */
    private final PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();

    /**
     * Tags configuration for export testing.
     */
    private final TagsConfiguration tagsConfiguration = new TagsConfiguration();

    /**
     * Tests exporting a collection.
     */
    @Test
    @SneakyThrows
    void testExportCollection() {
        String exportId = "000-xyz";
        prepareExportTest(exportId);

        Menu menu = prepareMenuExportTest();
        Item item = prepareItemExportTest();

        when(searchService.search(any(), anyInt())).thenReturn(List.of(item));

        Path coverPicture = Path.of("test/exports/000-xyz.jpg");
        lenient().when(fileRepository.exists(coverPicture)).thenReturn(true);

        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setId(exportId);
        collectionExport.setTitle(new TranslatableString("title"));
        collectionExport.setDescription(new TranslatableString("description"));
        collectionExport.setCoverPictureExtension("jpg");

        exporter.exportCollection(collectionExport, menu, propertiesConfiguration, tagsConfiguration);

        verifyConfigFileHandling(1);
        verifyDirAndMainFileHandling("456-def", ContentSource.MENU);
        verifyMenuFileIsWritten(exportId, menu);
        verifyItemAndMediaFilesAreWritten(exportId, item);

        // Verify cover picture is copied to export:
        verify(fileRepository).copy(
                coverPicture,
                Path.of("test/exports/000-xyz.artivact.collection/cover-picture.jpg")
        );
    }

    /**
     * Tests exporting a menu.
     */
    @Test
    @SneakyThrows
    void testExportMenu() {
        String menuId = "456-def";

        prepareExportTest(menuId);
        Menu menu = prepareMenuExportTest();

        exporter.exportMenu(ExportConfiguration.builder().excludeItems(true).build(), menu, propertiesConfiguration, tagsConfiguration);

        verifyConfigFileHandling(0);
        verifyDirAndMainFileHandling(menuId, ContentSource.MENU);
        verifyMenuFileIsWritten(menuId, menu);
    }

    /**
     * Tests exporting an item.
     */
    @Test
    @SneakyThrows
    void testExportItem() {
        String itemId = "123-abc";

        prepareExportTest(itemId);
        Item item = prepareItemExportTest();

        exporter.exportItem(item, propertiesConfiguration, tagsConfiguration);

        verifyDirAndMainFileHandling(itemId, ContentSource.ITEM);
        verifyConfigFileHandling(1);
        verifyItemAndMediaFilesAreWritten(itemId, item);
    }

    /**
     * Tests exporting the property configuration.
     */
    @Test
    @SneakyThrows
    void testExportPropertiesConfiguration() {
        Path root = Path.of("test");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        Path exportedTagsConfiguration = exporter.exportPropertiesConfiguration(propertiesConfiguration);

        assertThat(exportedTagsConfiguration).isEqualTo(Path.of("test/exports/artivact.properties-configuration.json"));
        verify(objectMapper).writeValue(any(File.class), eq(propertiesConfiguration));
    }

    /**
     * Tests exporting the tags configuration.
     */
    @Test
    @SneakyThrows
    void testExportTagsConfiguration() {
        Path root = Path.of("test");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        Path exportedTagsConfiguration = exporter.exportTagsConfiguration(tagsConfiguration);

        assertThat(exportedTagsConfiguration).isEqualTo(Path.of("test/exports/artivact.tags-configuration.json"));
        verify(objectMapper).writeValue(any(File.class), eq(tagsConfiguration));
    }

    /**
     * Prepare an export test.
     *
     * @param id The export's ID.
     */
    private void prepareExportTest(String id) {
        Path root = Path.of("test");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        exportDir = Path.of("test/exports/" + id + ".artivact.collection");
        exportFile = Path.of("test/exports/" + id + ".artivact.collection.zip");

        when(fileRepository.exists(exportDir)).thenReturn(true);
        when(fileRepository.exists(exportFile)).thenReturn(true);
    }

    /**
     * Prepares an item export.
     *
     * @return A newly created {@link Item}.
     */
    private Item prepareItemExportTest() {
        Item item = new Item();
        item.setId("123-abc");
        item.setTitle(new TranslatableString("title"));
        item.setDescription(new TranslatableString());
        item.getMediaContent().getModels().add("model.glb");
        item.getMediaContent().getImages().add("image.jpg");

        Path itemDir = Path.of("itemDir");
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(itemDir);

        return item;
    }

    /**
     * Prepares a menu export.
     *
     * @return A newly created {@link Menu}.
     */
    private Menu prepareMenuExportTest() {
        Menu menu = new Menu();
        menu.setId("456-def");
        menu.setTargetPageId("789-ghi");

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(List.of(
                new AvatarWidget(),
                new ImageTextWidget(),
                new InfoBoxWidget(),
                new PageTitleWidget(),
                new ItemSearchWidget(),
                new TextWidget()
        ));
        when(pageService.loadPageContent(eq("789-ghi"), anySet())).thenReturn(pageContent);

        return menu;
    }

    /**
     * Verifies general export directory handling and that the main file is written with the correct values.
     *
     * @param expectedSourceId      The expected source ID from the export's main file.
     * @param expectedContentSource The expected content source value from the export's main file.
     */
    @SneakyThrows
    private void verifyDirAndMainFileHandling(String expectedSourceId, ContentSource expectedContentSource) {
        // Verify old export data is deleted before export and dir is cleaned up afterward:
        verify(fileRepository, times(2)).delete(exportDir);
        verify(fileRepository).delete(exportFile);
        verify(fileRepository).createDirIfRequired(exportDir);

        // Verify main file is written:
        ArgumentCaptor<ExchangeMainData> argCap = ArgumentCaptor.forClass(ExchangeMainData.class);
        verify(objectMapper).writeValue(eq(exportDir.resolve("artivact.content.json").toFile()), argCap.capture());
        assertThat(argCap.getValue().getSourceId()).isEqualTo(expectedSourceId);
        assertThat(argCap.getValue().getContentSource()).isEqualTo(expectedContentSource);
        assertThat(argCap.getValue().getSchemaVersion()).isEqualTo(1);

        // Verify result is packed:
        verify(fileRepository).pack(exportDir, exportFile);
    }

    /**
     * Verifies that the properties and tags configuration files are written or not.
     *
     * @param expectedNumberOfConfigExportInvocations Set to {@code 0} to verify config files are NOT written, to {@code 1} otherwise.
     */
    @SneakyThrows
    private void verifyConfigFileHandling(int expectedNumberOfConfigExportInvocations) {
        verify(objectMapper, times(expectedNumberOfConfigExportInvocations))
                .writeValue(exportDir.resolve("artivact.properties-configuration.json").toFile(), propertiesConfiguration);
        verify(objectMapper, times(expectedNumberOfConfigExportInvocations))
                .writeValue(exportDir.resolve("artivact.tags-configuration.json").toFile(), tagsConfiguration);
    }

    /**
     * Verifies item files are written and media files are copied:
     *
     * @param exportId The export's ID.
     * @param item     The exported item.
     */
    @SneakyThrows
    private void verifyItemAndMediaFilesAreWritten(String exportId, Item item) {
        verify(objectMapper).writeValue(new File("test/exports/" + exportId + ".artivact.collection/123-abc/artivact.item.json"), item);
        verify(fileRepository).copy(Path.of("itemDir/models/model.glb"), Path.of("test/exports/" + exportId + ".artivact.collection/123-abc/model.glb"));
        verify(fileRepository).copy(Path.of("itemDir/images/image.jpg"), Path.of("test/exports/" + exportId + ".artivact.collection/123-abc/image.jpg"));
    }

    /**
     * Verifies that the menu file is written.
     *
     * @param exportId The export's ID.
     * @param menu     The exported menu.
     */
    @SneakyThrows
    private void verifyMenuFileIsWritten(String exportId, Menu menu) {
        verify(objectMapper).writeValue(new File("test/exports/" + exportId + ".artivact.collection/456-def.artivact.menu.json"), menu);
    }

}
