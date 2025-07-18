package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadExchangeConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.ExportItemUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.item.UploadItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.out.gateway.ArtivactGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.ExchangeConfiguration;
import com.arassec.artivact.domain.model.item.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadItemService implements UploadItemUseCase {

    private final ExportItemUseCase exportItemUseCase;

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    private final LoadItemUseCase loadItemUseCase;

    private final SaveItemUseCase saveItemUseCase;

    private final LoadExchangeConfigurationUseCase loadExchangeConfigurationUseCase;

    private final FileRepository fileRepository;

    private final ArtivactGateway artivactApiAdapter;

    /**
     * Exports the item with the given ID and uploads it to a remote application instance configured in the exchange
     * configuration.
     *
     * @param itemId       The item's ID.
     * @param asynchronous Set to {@code true} to upload the item in a different thread.
     */
    public synchronized void uploadItemToRemoteInstance(String itemId, boolean asynchronous) {
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

        Path exportFile = exportItemUseCase.exportItem(itemId);

        if (asynchronous) {
            runBackgroundOperationUseCase.execute("itemUpload", "uploading", progressMonitor -> uploadItem(remoteServer, apiToken, itemId, exportFile));
        } else {
            uploadItem(remoteServer, apiToken, itemId, exportFile);
        }
    }

    /**
     * Uploads an item to a remote Artivact instance.
     *
     * @param remoteServer The remote artivact instance.
     * @param apiToken     API-Token for authentication and authorization.
     * @param itemId       The item's ID.
     * @param exportFile   The export file containing the exported item.
     */
    private void uploadItem(String remoteServer, String apiToken, String itemId, Path exportFile) {
        try {
            artivactApiAdapter.importItem(remoteServer, apiToken, exportFile);
            Item item = loadItemUseCase.load(itemId).orElseThrow();
            item.setSyncVersion(item.getVersion() + 1); // Saving the item will increase its version by one!
            saveItemUseCase.save(item);
        } finally {
            if (fileRepository.exists(exportFile)) {
                fileRepository.delete(exportFile);
            }
        }
    }

}
