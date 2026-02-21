package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.account.LoadAccountUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.account.Account;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.ITEM_EXCHANGE_FILENAME_JSON;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemImportServiceTest {

    @InjectMocks
    private ItemImportService service;

    @Mock
    private SaveItemUseCase saveItemUseCase;

    @Mock
    private LoadAccountUseCase loadAccountUseCase;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;

    @Mock
    private ImportTagsConfigurationUseCase importTagsConfigurationUseCase;

    @Test
    void testImportItemWithApiTokenFailsOnEmptyToken() {
        // Given
        Path contentExport = Path.of("export.artivact.collection.zip");

        // When / Then
        assertThatThrownBy(() -> service.importItem(contentExport, ""))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("API token cannot be empty!");
    }

    @Test
    void testImportItemWithApiTokenFailsOnNullToken() {
        // Given
        Path contentExport = Path.of("export.artivact.collection.zip");

        // When / Then
        assertThatThrownBy(() -> service.importItem(contentExport, null))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("API token cannot be empty!");
    }

    @Test
    void testImportItemWithApiTokenFailsOnMissingAccount() {
        // Given
        Path contentExport = Path.of("export.artivact.collection.zip");
        when(loadAccountUseCase.loadByApiToken("valid-token")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.importItem(contentExport, "valid-token"))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void testImportItemWithApiTokenFailsOnUnauthorizedAccount() {
        // Given
        Path contentExport = Path.of("export.artivact.collection.zip");
        Account account = new Account();
        account.setUser(false);
        account.setAdmin(false);

        when(loadAccountUseCase.loadByApiToken("valid-token")).thenReturn(Optional.of(account));

        // When / Then
        assertThatThrownBy(() -> service.importItem(contentExport, "valid-token"))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Item import not allowed!");
    }

    @Test
    void testImportItemWithApiTokenSucceedsForUserAccount() {
        // Given
        Path contentExport = Path.of("export.artivact.collection.zip");
        Account account = new Account();
        account.setUser(true);
        account.setAdmin(false);

        when(loadAccountUseCase.loadByApiToken("valid-token")).thenReturn(Optional.of(account));
        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("temp"));

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(ContentSource.ITEM);
        exchangeMainData.setSourceIds(List.of());

        Path importDir = Path.of("temp", "export.artivact.collection");
        when(jsonMapper.readValue(eq(importDir.resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile()), eq(ExchangeMainData.class)))
                .thenReturn(exchangeMainData);

        // When
        service.importItem(contentExport, "valid-token");

        // Then
        verify(fileRepository).unpack(contentExport, importDir);
        verify(importPropertiesConfigurationUseCase).importPropertiesConfiguration(any(ImportContext.class));
        verify(importTagsConfigurationUseCase).importTagsConfiguration(any(ImportContext.class));
        verify(fileRepository).delete(importDir);
    }

    @Test
    void testImportItemFromPathSuccessfully() {
        // Given
        Path contentExport = Path.of("item-export.artivact.collection.zip");
        Path importDir = Path.of("temp", "item-export.artivact.collection");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("temp"));

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(ContentSource.ITEM);
        exchangeMainData.setSourceIds(List.of("item-123"));

        when(jsonMapper.readValue(eq(importDir.resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile()), eq(ExchangeMainData.class)))
                .thenReturn(exchangeMainData);

        Path itemSourceDir = Path.of("item-source-dir");
        when(fileRepository.getDirFromId(eq(importDir.resolve(DirectoryDefinitions.ITEMS_DIR)), eq("item-123")))
                .thenReturn(itemSourceDir);
        when(fileRepository.exists(itemSourceDir.resolve(ITEM_EXCHANGE_FILENAME_JSON))).thenReturn(true);

        Item item = new Item();
        when(fileRepository.read(itemSourceDir.resolve(ITEM_EXCHANGE_FILENAME_JSON))).thenReturn("{}");
        when(jsonMapper.readValue("{}", Item.class)).thenReturn(item);

        Path itemTargetDir = Path.of("item-target-dir");
        Path itemsDir = Path.of("items");
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(itemsDir);
        when(fileRepository.getDirFromId(itemsDir, "item-123")).thenReturn(itemTargetDir);

        // When
        service.importItem(contentExport);

        // Then
        verify(fileRepository).unpack(contentExport, importDir);
        verify(fileRepository).createDirIfRequired(itemTargetDir);
        verify(fileRepository).copy(itemSourceDir.resolve(DirectoryDefinitions.IMAGES_DIR), itemTargetDir.resolve(DirectoryDefinitions.IMAGES_DIR));
        verify(fileRepository).copy(itemSourceDir.resolve(DirectoryDefinitions.MODELS_DIR), itemTargetDir.resolve(DirectoryDefinitions.MODELS_DIR));
        verify(saveItemUseCase).save(item);
        verify(importPropertiesConfigurationUseCase).importPropertiesConfiguration(any(ImportContext.class));
        verify(importTagsConfigurationUseCase).importTagsConfiguration(any(ImportContext.class));
        verify(fileRepository).delete(importDir);
    }

    @Test
    void testImportItemFromPathFailsOnInvalidContentSource() {
        // Given
        Path contentExport = Path.of("invalid.artivact.collection.zip");
        Path importDir = Path.of("temp", "invalid.artivact.collection");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("temp"));

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(ContentSource.COLLECTION);
        exchangeMainData.setSourceIds(List.of());

        when(jsonMapper.readValue(eq(importDir.resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile()), eq(ExchangeMainData.class)))
                .thenReturn(exchangeMainData);

        // When / Then
        assertThatThrownBy(() -> service.importItem(contentExport))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Could not import item!");
    }

    @Test
    void testImportItemFromPathWrapsExceptionInArtivactException() {
        // Given
        Path contentExport = Path.of("failing.artivact.collection.zip");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("temp"));
        when(jsonMapper.readValue(any(File.class), eq(ExchangeMainData.class)))
                .thenThrow(new RuntimeException("read error"));

        // When / Then
        assertThatThrownBy(() -> service.importItem(contentExport))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Could not import item!");
    }

    @Test
    void testImportItemByIdSkipsWhenJsonFileNotFound() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import-dir"))
                .build();

        Path itemSourceDir = Path.of("item-source-dir");
        when(fileRepository.getDirFromId(eq(importContext.getImportDir().resolve(DirectoryDefinitions.ITEMS_DIR)), eq("missing-item")))
                .thenReturn(itemSourceDir);
        when(fileRepository.exists(itemSourceDir.resolve(ITEM_EXCHANGE_FILENAME_JSON))).thenReturn(false);

        // When
        service.importItem(importContext, "missing-item");

        // Then
        verify(fileRepository, never()).read(any());
        verify(saveItemUseCase, never()).save(any());
        verify(fileRepository, never()).createDirIfRequired(any());
    }

    @Test
    void testImportItemByIdCopiesImagesAndModels() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import-dir"))
                .build();

        Path itemSourceDir = Path.of("item-source-dir");
        when(fileRepository.getDirFromId(eq(importContext.getImportDir().resolve(DirectoryDefinitions.ITEMS_DIR)), eq("item-456")))
                .thenReturn(itemSourceDir);
        when(fileRepository.exists(itemSourceDir.resolve(ITEM_EXCHANGE_FILENAME_JSON))).thenReturn(true);

        Item item = new Item();
        when(fileRepository.read(itemSourceDir.resolve(ITEM_EXCHANGE_FILENAME_JSON))).thenReturn("{\"id\":\"item-456\"}");
        when(jsonMapper.readValue("{\"id\":\"item-456\"}", Item.class)).thenReturn(item);

        Path itemTargetDir = Path.of("item-target-dir");
        Path itemsDir = Path.of("items");
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(itemsDir);
        when(fileRepository.getDirFromId(itemsDir, "item-456")).thenReturn(itemTargetDir);

        // When
        service.importItem(importContext, "item-456");

        // Then
        verify(fileRepository).createDirIfRequired(itemTargetDir);
        verify(fileRepository).copy(
                itemSourceDir.resolve(DirectoryDefinitions.IMAGES_DIR),
                itemTargetDir.resolve(DirectoryDefinitions.IMAGES_DIR)
        );
        verify(fileRepository).copy(
                itemSourceDir.resolve(DirectoryDefinitions.MODELS_DIR),
                itemTargetDir.resolve(DirectoryDefinitions.MODELS_DIR)
        );
        verify(saveItemUseCase).save(item);
    }
}
