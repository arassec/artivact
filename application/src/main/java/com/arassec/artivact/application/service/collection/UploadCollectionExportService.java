package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.port.in.collection.UploadCollectionExportUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadExchangeConfigurationUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.gateway.ArtivactGateway;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.ExchangeConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.COLLECTION_EXCHANGE_SUFFIX;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.ZIP_FILE_SUFFIX;

/**
 * Service for uploading collection exports to remote Artivact instances.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadCollectionExportService implements UploadCollectionExportUseCase {

    /**
     * Use case for loading the exchange configuration.
     */
    private final LoadExchangeConfigurationUseCase loadExchangeConfigurationUseCase;

    /**
     * Use case for running background operations.
     */
    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    /**
     * Use case for accessing project directories.
     */
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * The artivact API gateway.
     */
    private final ArtivactGateway artivactGateway;

    /**
     * Uploads a collection export to the configured remote Artivact instance.
     *
     * @param collectionExportId The collection export's ID.
     */
    @Override
    public void uploadCollectionExportToRemoteInstance(String collectionExportId) {
        ExchangeConfiguration exchangeConfiguration = loadExchangeConfigurationUseCase.loadExchangeConfiguration();
        String remoteServerConfig = exchangeConfiguration.getRemoteServer();
        String apiToken = exchangeConfiguration.getApiToken();

        if (!StringUtils.hasText(remoteServerConfig) || !StringUtils.hasText(apiToken)) {
            throw new ArtivactException("Remote instance access is not configured properly!");
        }

        String remoteServer;
        if (!remoteServerConfig.endsWith("/")) {
            remoteServer = remoteServerConfig + "/";
        } else {
            remoteServer = remoteServerConfig;
        }

        Path exportFile = useProjectDirsUseCase.getExportsDir()
                .resolve(collectionExportId + COLLECTION_EXCHANGE_SUFFIX + ZIP_FILE_SUFFIX);

        runBackgroundOperationUseCase.execute("collectionExportUpload", "uploading",
                progressMonitor -> {
                    artivactGateway.importCollectionForDistribution(remoteServer, apiToken, exportFile);
                    progressMonitor.updateProgress(1, 1);
                });
    }

}
