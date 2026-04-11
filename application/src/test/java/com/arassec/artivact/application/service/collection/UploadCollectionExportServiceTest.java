package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.port.in.configuration.LoadExchangeConfigurationUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.gateway.ArtivactGateway;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.ExchangeConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadCollectionExportServiceTest {

    @Mock
    private LoadExchangeConfigurationUseCase loadExchangeConfigurationUseCase;

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private ArtivactGateway artivactGateway;

    @InjectMocks
    private UploadCollectionExportService service;

    @Test
    void testUploadCollectionExportToRemoteInstance() {
        String collectionExportId = "export-123";

        ExchangeConfiguration config = new ExchangeConfiguration();
        config.setRemoteServer("http://remote-server");
        config.setApiToken("secret-token");

        when(loadExchangeConfigurationUseCase.loadExchangeConfiguration()).thenReturn(config);
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("exports"));

        service.uploadCollectionExportToRemoteInstance(collectionExportId);

        verify(runBackgroundOperationUseCase).execute(eq("collectionExportUpload"), eq("uploading"), any());
    }

    @Test
    void testUploadCollectionExportToRemoteInstanceWithTrailingSlash() {
        String collectionExportId = "export-456";

        ExchangeConfiguration config = new ExchangeConfiguration();
        config.setRemoteServer("http://remote-server/");
        config.setApiToken("secret-token");

        when(loadExchangeConfigurationUseCase.loadExchangeConfiguration()).thenReturn(config);
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("exports"));

        service.uploadCollectionExportToRemoteInstance(collectionExportId);

        verify(runBackgroundOperationUseCase).execute(eq("collectionExportUpload"), eq("uploading"), any());
    }

    @Test
    void testUploadCollectionExportToRemoteInstanceThrowsIfConfigInvalid() {
        ExchangeConfiguration config = new ExchangeConfiguration();
        config.setRemoteServer("");
        config.setApiToken(null);

        when(loadExchangeConfigurationUseCase.loadExchangeConfiguration()).thenReturn(config);

        assertThatThrownBy(() -> service.uploadCollectionExportToRemoteInstance("id1"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Remote instance access is not configured");
    }

}
