package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.account.LoadAccountUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.ManageItemImagesUseCase;
import com.arassec.artivact.application.port.in.item.ManageItemModelsUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.account.Account;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.ITEM_EXCHANGE_FILENAME_JSON;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemImportServiceTest {

    @Mock
    private ManageItemImagesUseCase manageItemImagesUseCase;

    @Mock
    private ManageItemModelsUseCase manageItemModelsUseCase;

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

    @InjectMocks
    private ItemImportService itemImportService;

    private final Path zipPath = Path.of("content.zip");
    private final Path tempDir = Path.of("tmp");
    private final Path importDir = tempDir.resolve("content");

    @BeforeEach
    void setup() {
        lenient().when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);
    }

    @Test
    void testImportItemWithInvalidTokenThrows() {
        assertThatThrownBy(() -> itemImportService.importItem(zipPath, ""))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("API token cannot be empty!");
    }

    @Test
    void testImportItemWithUnauthorizedAccountThrows() {
        Account account = new Account();
        account.setUser(false);
        account.setAdmin(true);
        when(loadAccountUseCase.loadByApiToken("token")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> itemImportService.importItem(zipPath, "token"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Item import not allowed!");
    }

    @Test
    void testImportItemWithValidTokenCallsImportItem() {
        Account account = new Account();
        account.setUser(true);
        account.setAdmin(false);
        when(loadAccountUseCase.loadByApiToken("token")).thenReturn(Optional.of(account));

        ItemImportService spyService = spy(itemImportService);

        doNothing().when(spyService).importItem(any(Path.class));
        spyService.importItem(zipPath, "token");
        verify(spyService).importItem(zipPath);
    }

    @Test
    void testImportItemHappyPath() {
        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setSourceId("item1");
        when(jsonMapper.readValue(any(File.class), eq(ExchangeMainData.class))).thenReturn(exchangeMainData);

        when(fileRepository.read(any(Path.class))).thenReturn("item-json");
        Item item = new Item();
        when(jsonMapper.readValue("item-json", Item.class)).thenReturn(item);

        itemImportService.importItem(zipPath);

        verify(fileRepository).unpack(eq(zipPath), any(Path.class));
        verify(importPropertiesConfigurationUseCase).importPropertiesConfiguration(any(ImportContext.class));
        verify(importTagsConfigurationUseCase).importTagsConfiguration(any(ImportContext.class));
        verify(saveItemUseCase).save(item);
        verify(fileRepository).delete(any());
    }

    @Test
    void testImportItemThrowsOnException() {
        when(jsonMapper.readValue(anyString(), eq(ExchangeMainData.class))).thenThrow(new RuntimeException("fail"));
        assertThatThrownBy(() -> itemImportService.importItem(zipPath))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not import item!");
    }

    @Test
    void testImportItemWithContextHappyPath() {
        ImportContext ctx = ImportContext.builder().importDir(importDir).build();
        Path itemDir = importDir.resolve("item1");
        Item item = mock(Item.class);

        MediaContent mediaContent = mock(MediaContent.class);
        when(fileRepository.read(itemDir.resolve(ITEM_EXCHANGE_FILENAME_JSON))).thenReturn("{}");
        when(jsonMapper.readValue(anyString(), eq(Item.class))).thenReturn(item);
        when(item.getMediaContent()).thenReturn(mediaContent);
        when(mediaContent.getImages()).thenReturn(List.of("img1.png"));
        when(mediaContent.getModels()).thenReturn(List.of("model1.obj"));
        when(fileRepository.readStream(any())).thenReturn(mock(InputStream.class));

        itemImportService.importItem(ctx, "item1");

        verify(manageItemImagesUseCase).saveImage(any(), any(), any(), eq(true));
        verify(manageItemModelsUseCase).saveModel(any(), any(), any(), eq(true));
        verify(saveItemUseCase).save(item);
    }

    @Test
    void testImportItemWithContextExceptionThrows() {
        ImportContext ctx = ImportContext.builder().importDir(importDir).build();
        when(fileRepository.read(any())).thenReturn("broken");
        when(jsonMapper.readValue(anyString(), eq(Item.class))).thenThrow(new RuntimeException("fail") {
        });

        assertThatThrownBy(() -> itemImportService.importItem(ctx, "id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("fail");
    }

}
