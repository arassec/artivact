package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.core.model.exchange.ContentSource;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.exporter.ItemExporter;
import com.arassec.artivact.domain.exchange.exporter.MenuExporter;
import com.arassec.artivact.domain.exchange.exporter.PropertiesExporter;
import com.arassec.artivact.domain.exchange.exporter.TagsExporter;
import com.arassec.artivact.domain.exchange.model.ExchangeMainData;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ArtivactStandardExporter}.
 */
@ExtendWith(MockitoExtension.class)
class ArtivactStandardExporterTest {

    /**
     * Tested exporter
     */
    @InjectMocks
    private ArtivactStandardExporter exporter;

    /**
     * Exporter for the property configuration.
     */
    @Mock
    private PropertiesExporter propertiesExporter;

    /**
     * Exporter for the tag configuration.
     */
    @Mock
    private TagsExporter tagsExporter;

    /**
     * Exporter for items.
     */
    @Mock
    private ItemExporter itemExporter;

    /**
     * Exporter for menus.
     */
    @Mock
    private MenuExporter menuExporter;

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
     * Export directory used during tests.
     */
    private Path exportDir;

    /**
     * Export file used during tests.
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
    void testExportCollection() {
        String exportId = "666-ase";

        initializeCreateExportContext();
        initializePrepareExport(exportId, true);

        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setId(exportId);
        collectionExport.setTitle(new TranslatableString("collection-title"));
        collectionExport.setDescription(new TranslatableString("collection-description"));
        collectionExport.setCoverPictureExtension("JPG");

        Menu menu = new Menu();
        menu.setId("456-def");

        Path coverPicturePath = Paths.get("root/exports/666-ase.JPG");
        when(fileRepository.exists(coverPicturePath)).thenReturn(true);

        exporter.exportCollection(collectionExport, menu, propertiesConfiguration, tagsConfiguration);

        verify(menuExporter).exportMenu(any(ExportContext.class), eq(menu));

        verify(fileRepository).copy(coverPicturePath,
                Path.of("root/exports/666-ase.artivact.collection/cover-picture.JPG"));

        ExchangeMainData exchangeMainData = verifyExportMainData(exportId, ContentSource.MENU);
        assertThat(exchangeMainData.getTitle().getValue()).isEqualTo("collection-title");
        assertThat(exchangeMainData.getDescription().getValue()).isEqualTo("collection-description");

        verifyConfigExport();
        verifyFileHandling(true);
    }

    /**
     * Tests exporting a menu.
     */
    @Test
    void testExportMenu() {
        String exportId = "456-def";

        initializeCreateExportContext();
        initializePrepareExport(exportId, true);

        Menu menu = new Menu();
        menu.setId("456-def");

        exporter.exportMenu(menu);

        verify(menuExporter).exportMenu(any(ExportContext.class), eq(menu));

        ExchangeMainData exchangeMainData = verifyExportMainData(exportId, ContentSource.MENU);
        assertThat(exchangeMainData.getTitle().getValue()).isNull();
        assertThat(exchangeMainData.getDescription().getValue()).isNull();

        verifyFileHandling(true);
    }

    /**
     * Tests exporting an item.
     */
    @Test
    void testExportItem() {
        String exportId = "123-abc";

        initializeCreateExportContext();
        initializePrepareExport(exportId, false);

        Item item = new Item();
        item.setId(exportId);
        item.setTitle(new TranslatableString("item-title"));
        item.setDescription(new TranslatableString("item-description"));

        exporter.exportItem(item, propertiesConfiguration, tagsConfiguration);

        verify(itemExporter).exportItem(any(ExportContext.class), eq(item));

        ExchangeMainData exchangeMainData = verifyExportMainData(exportId, ContentSource.ITEM);
        assertThat(exchangeMainData.getTitle().getValue()).isEqualTo("item-title");
        assertThat(exchangeMainData.getDescription().getValue()).isEqualTo("item-description");

        verifyConfigExport();
        verifyFileHandling(false);
    }

    /**
     * Initializes the test environment for creating the export context.
     */
    private void initializeCreateExportContext() {
        Path root = Path.of("root");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);
    }

    /**
     * Initializes the test environment for preparing the export.
     *
     * @param exportId        The export's ID.
     * @param fileAndDirExist Set to {@code true} if export dir and file should be tested as already existing.
     */
    private void initializePrepareExport(String exportId, boolean fileAndDirExist) {
        exportDir = Path.of("root/exports/" + exportId + ".artivact.collection");
        when(fileRepository.exists(exportDir)).thenReturn(fileAndDirExist);
        exportFile = Path.of("root/exports/" + exportId + ".artivact.collection.zip");
        when(fileRepository.exists(exportFile)).thenReturn(fileAndDirExist);
    }

    /**
     * Verifies the general handling of the export's main data file.
     *
     * @param exportId              The export's ID.
     * @param expectedContentSource The expected content source set in the export's main data file.
     * @return The captured {@link ExchangeMainData} for further verification.
     */
    @SneakyThrows
    private ExchangeMainData verifyExportMainData(String exportId, ContentSource expectedContentSource) {
        ArgumentCaptor<ExchangeMainData> argCap = ArgumentCaptor.forClass(ExchangeMainData.class);

        verify(objectMapper).writeValue(
                eq(Path.of("root/exports/" + exportId + ".artivact.collection/artivact.content.json").toFile()),
                argCap.capture()
        );
        ExchangeMainData exchangeMainData = argCap.getValue();
        assertThat(exchangeMainData.getContentSource()).isEqualTo(expectedContentSource);

        return exchangeMainData;
    }

    /**
     * Verifies that configurations have been exported.
     */
    private void verifyConfigExport() {
        verify(propertiesExporter).exportPropertiesConfiguration(any(ExportContext.class), eq(propertiesConfiguration));
        verify(tagsExporter).exportTagsConfiguration(any(ExportContext.class), eq(tagsConfiguration));
    }

    /**
     * Verifies export dir and file handling.
     *
     * @param fileAndDirExist Set to {@code true} if the export dir and file were tested as already existing.
     */
    private void verifyFileHandling(boolean fileAndDirExist) {
        verify(fileRepository, times(fileAndDirExist ? 2 : 1)).delete(exportDir);
        verify(fileRepository, times(fileAndDirExist ? 1 : 0)).delete(exportFile);
        verify(fileRepository).pack(exportDir, exportFile);
    }

}
