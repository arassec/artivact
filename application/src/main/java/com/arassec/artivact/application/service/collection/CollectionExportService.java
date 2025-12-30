package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.port.in.collection.ExportCollectionUseCase;
import com.arassec.artivact.application.port.in.configuration.ExportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ExportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.ExportMenuUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.service.BaseExportService;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.menu.Menu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Service for collection export.
 */
public class CollectionExportService extends BaseExportService implements ExportCollectionUseCase {

    /**
     * The application's file repository.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * The json mapper.
     */
    @Getter
    private final JsonMapper jsonMapper;

    /**
     * Use case for use project dirs.
     */
    @Getter
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Use case for export properties configuration.
     */
    private final ExportPropertiesConfigurationUseCase exportPropertiesConfigurationUseCase;

    /**
     * Use case for export tags configuration.
     */
    private final ExportTagsConfigurationUseCase exportTagsConfigurationUseCase;

    /**
     * Use case for export menu.
     */
    private final ExportMenuUseCase exportMenuUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportCollection(CollectionExport collectionExport, Menu menu, PropertiesConfiguration propertiesConfiguration, TagsConfiguration tagsConfiguration) {
        ExportContext exportContext = createExportContext(collectionExport.getId(), collectionExport.getExportConfiguration());
        exportContext.setId(collectionExport.getId());
        exportContext.setCoverPictureExtension(collectionExport.getCoverPictureExtension());

        prepareExport(exportContext);

        exportMainData(exportContext, ContentSource.MENU, menu.getId(), collectionExport.getTitle(), collectionExport.getDescription(), collectionExport.getContent());

        exportPropertiesConfigurationUseCase.exportPropertiesConfiguration(exportContext, propertiesConfiguration);
        exportTagsConfigurationUseCase.exportTagsConfiguration(exportContext, tagsConfiguration);

        exportMenuUseCase.exportMenu(exportContext, menu);

        if (StringUtils.hasText(collectionExport.getCoverPictureExtension())) {
            Path coverPictureFile = useProjectDirsUseCase.getExportsDir()
                    .resolve(collectionExport.getId() + "." + collectionExport.getCoverPictureExtension());
            if (fileRepository.exists(coverPictureFile)) {
                fileRepository.copy(coverPictureFile, exportContext.getExportDir()
                        .resolve("cover-picture." + collectionExport.getCoverPictureExtension()), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        fileRepository.pack(exportContext.getExportDir(), exportContext.getExportFile());
        fileRepository.delete(exportContext.getExportDir());

        return exportContext.getExportFile();
    }

}
