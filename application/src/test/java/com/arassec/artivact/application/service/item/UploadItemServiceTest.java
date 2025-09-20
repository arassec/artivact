package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadExchangeConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.ExportItemUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.out.gateway.ArtivactGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.ExchangeConfiguration;
import com.arassec.artivact.domain.model.item.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadItemServiceTest {

    @Mock
    private ExportItemUseCase exportItemUseCase;

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @Mock
    private LoadItemUseCase loadItemUseCase;

    @Mock
    private SaveItemUseCase saveItemUseCase;

    @Mock
    private LoadExchangeConfigurationUseCase loadExchangeConfigurationUseCase;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ArtivactGateway artivactGateway;

    @InjectMocks
    private UploadItemService service;

    @Test
    void testUploadItemToRemoteInstanceSynchronously() {
        String itemId = "123";
        Path exportFile = Path.of("export.zip");

        ExchangeConfiguration config = new ExchangeConfiguration();
        config.setRemoteServer("http://remote-server");
        config.setApiToken("secret-token");

        Item item = new Item();
        item.setId(itemId);
        item.setVersion(5);

        when(loadExchangeConfigurationUseCase.loadExchangeConfiguration()).thenReturn(config);
        when(exportItemUseCase.exportItem(itemId)).thenReturn(exportFile);
        when(loadItemUseCase.load(itemId)).thenReturn(Optional.of(item));
        when(fileRepository.exists(exportFile)).thenReturn(true);

        service.uploadItemToRemoteInstance(itemId, false);

        verify(artivactGateway).importItem("http://remote-server/", "secret-token", exportFile);
        verify(saveItemUseCase).save(argThat(saved -> saved.getSyncVersion() == 6));
        verify(fileRepository).delete(exportFile);
    }

    @Test
    void testUploadItemToRemoteInstanceAsynchronously() {
        String itemId = "456";
        Path exportFile = Path.of("export.zip");

        ExchangeConfiguration config = new ExchangeConfiguration();
        config.setRemoteServer("http://remote-server/");
        config.setApiToken("secret-token");

        when(loadExchangeConfigurationUseCase.loadExchangeConfiguration()).thenReturn(config);
        when(exportItemUseCase.exportItem(itemId)).thenReturn(exportFile);

        service.uploadItemToRemoteInstance(itemId, true);

        verify(runBackgroundOperationUseCase).execute(eq("itemUpload"), eq("uploading"), any());
    }

    @Test
    void testUploadItemToRemoteInstanceThrowsIfConfigInvalid() {
        ExchangeConfiguration config = new ExchangeConfiguration();
        config.setRemoteServer(""); // leer
        config.setApiToken(null);

        when(loadExchangeConfigurationUseCase.loadExchangeConfiguration()).thenReturn(config);

        assertThatThrownBy(() -> service.uploadItemToRemoteInstance("id1", false))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Remote instance access is not configured");
    }

    @Test
    void testExportFileIsDeletedInFinallyEvenOnException() {
        String itemId = "789";
        Path exportFile = Path.of("export.zip");

        ExchangeConfiguration config = new ExchangeConfiguration();
        config.setRemoteServer("http://remote-server/");
        config.setApiToken("secret-token");

        Item item = new Item();
        item.setId(itemId);

        when(loadExchangeConfigurationUseCase.loadExchangeConfiguration()).thenReturn(config);
        when(exportItemUseCase.exportItem(itemId)).thenReturn(exportFile);
        when(fileRepository.exists(exportFile)).thenReturn(true);

        doThrow(new RuntimeException("upload failed"))
                .when(artivactGateway).importItem(any(), any(), any());

        assertThatThrownBy(() -> service.uploadItemToRemoteInstance(itemId, false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("upload failed");

        verify(fileRepository).delete(exportFile); // trotzdem gelöscht
    }

}
