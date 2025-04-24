package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.exchange.ContentSource;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.importer.ItemImporter;
import com.arassec.artivact.domain.exchange.importer.MenuImporter;
import com.arassec.artivact.domain.exchange.importer.PropertiesImporter;
import com.arassec.artivact.domain.exchange.importer.TagsImporter;
import com.arassec.artivact.domain.exchange.model.ExchangeMainData;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ArtivactStandardImporter}.
 */
@ExtendWith(MockitoExtension.class)
class ArtivactStandardImporterTest {

    /**
     * Importer under test.
     */
    @InjectMocks
    private ArtivactStandardImporter importer;

    /**
     * Importer for items.
     */
    @Mock
    private ItemImporter itemImporter;

    /**
     * Importer for menus.
     */
    @Mock
    private MenuImporter menuImporter;

    /**
     * Importer for the property configuration.
     */
    @Mock
    private PropertiesImporter propertiesImporter;

    /**
     * Importer for the tag configuration.
     */
    @Mock
    private TagsImporter tagsImporter;

    /**
     * Provider for project data.
     */
    @Mock
    private ProjectDataProvider projectDataProvider;

    /**
     * The application's {@link FileRepository}.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * The object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Tests importing an exported item.
     */
    @Test
    @SneakyThrows
    void testImportContentItem() {
        Path root = Path.of("root");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        Path importFile = Path.of("content-export.zip");
        Path importDir = Path.of("root/temp/content-export");

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(ContentSource.ITEM);
        exchangeMainData.setSourceId("123-abc");
        when(objectMapper.readValue(Path.of("root/temp/content-export/artivact.content.json").toFile(),
                ExchangeMainData.class)).thenReturn(exchangeMainData);

        importer.importContent(importFile);

        verify(fileRepository).unpack(importFile, importDir);

        verify(itemImporter).importItem(any(ImportContext.class), eq("123-abc"));

        verify(propertiesImporter).importPropertiesConfiguration(any(ImportContext.class));
        verify(tagsImporter).importTagsConfiguration(any(ImportContext.class));

        verify(fileRepository).delete(importDir);
    }

    /**
     * Tests importing an exported menu.
     */
    @Test
    @SneakyThrows
    void testImportContentMenu() {
        Path root = Path.of("root");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        Path importFile = Path.of("content-export.zip");
        Path importDir = Path.of("root/temp/content-export");

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(ContentSource.MENU);
        exchangeMainData.setSourceId("456-def");
        when(objectMapper.readValue(Path.of("root/temp/content-export/artivact.content.json").toFile(),
                ExchangeMainData.class)).thenReturn(exchangeMainData);

        importer.importContent(importFile);

        verify(fileRepository).unpack(importFile, importDir);

        verify(menuImporter).importMenu(any(ImportContext.class), eq("456-def"), eq(true));

        verify(propertiesImporter).importPropertiesConfiguration(any(ImportContext.class));
        verify(tagsImporter).importTagsConfiguration(any(ImportContext.class));

        verify(fileRepository).delete(importDir);
    }

    /**
     * Tests importing a collection export.
     */
    @Test
    @SneakyThrows
    void testImportCollection() {
        Path root = Path.of("root");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        Path importFile = Path.of("collection-export.zip");
        Path importDir = Path.of("root/temp/collection-export");
        Path coverPicture = Path.of("root/temp/collection-export/cover-picture.PNG");

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setId("789-xyz");
        exchangeMainData.setContentSource(ContentSource.MENU);
        exchangeMainData.setSourceId("456-def");
        exchangeMainData.setCoverPictureExtension("PNG");
        when(objectMapper.readValue(Path.of("root/temp/collection-export/artivact.content.json").toFile(),
                ExchangeMainData.class)).thenReturn(exchangeMainData);

        when(fileRepository.exists(coverPicture)).thenReturn(true);

        importer.importCollection(importFile, false);

        verify(fileRepository).unpack(importFile, importDir);

        verify(propertiesImporter).importPropertiesConfiguration(any(ImportContext.class));
        verify(tagsImporter).importTagsConfiguration(any(ImportContext.class));
        verify(menuImporter).importMenu(any(ImportContext.class), eq("456-def"), eq(true));

        // Copy export file:
        verify(fileRepository).copy(importFile, Path.of("root/exports/789-xyz.artivact.collection.zip"), StandardCopyOption.REPLACE_EXISTING);
        // Copy export cover picture:
        verify(fileRepository).copy(coverPicture, Path.of("root/exports/789-xyz.PNG"));

        verify(fileRepository).delete(importDir);
    }

    /**
     * Tests importing a collection export for distribution only.
     */
    @Test
    @SneakyThrows
    void testImportCollectionForDistribution() {
        Path root = Path.of("root");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        Path importFile = Path.of("collection-export.zip");
        Path importDir = Path.of("root/temp/collection-export");
        Path coverPicture = Path.of("root/temp/collection-export/cover-picture.PNG");

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setId("789-xyz");
        exchangeMainData.setContentSource(ContentSource.MENU);
        exchangeMainData.setSourceId("456-def");
        exchangeMainData.setCoverPictureExtension("PNG");
        when(objectMapper.readValue(Path.of("root/temp/collection-export/artivact.content.json").toFile(),
                ExchangeMainData.class)).thenReturn(exchangeMainData);

        when(fileRepository.exists(coverPicture)).thenReturn(true);

        importer.importCollection(importFile, true);

        verify(fileRepository).unpack(importFile, importDir);

        verify(propertiesImporter, times(0)).importPropertiesConfiguration(any(ImportContext.class));
        verify(tagsImporter, times(0)).importTagsConfiguration(any(ImportContext.class));
        verify(menuImporter, times(0)).importMenu(any(ImportContext.class), eq("456-def"), eq(true));

        // Copy export file:
        verify(fileRepository).copy(importFile, Path.of("root/exports/789-xyz.artivact.collection.zip"), StandardCopyOption.REPLACE_EXISTING);
        // Copy export cover picture:
        verify(fileRepository).copy(coverPicture, Path.of("root/exports/789-xyz.PNG"));

        verify(fileRepository).delete(importDir);
    }

    /**
     * Tests input validation when importing a collection export.
     */
    @Test
    @SneakyThrows
    void testImportCollectionInputValidation() {
        Path root = Path.of("root");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        Path importFile = Path.of("collection-export.zip");

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(ContentSource.ITEM);
        when(objectMapper.readValue(Path.of("root/temp/collection-export/artivact.content.json").toFile(),
                ExchangeMainData.class)).thenReturn(exchangeMainData);

        assertThrows(ArtivactException.class, () -> importer.importCollection(importFile, false));
    }

}
