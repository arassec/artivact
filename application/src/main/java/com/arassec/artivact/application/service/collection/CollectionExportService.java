package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.port.in.collection.ExportCollectionUseCase;
import com.arassec.artivact.application.port.in.menu.ExportMenuUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.service.BaseExportService;
import com.arassec.artivact.domain.model.TranslatableString;
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

/**
 * Service for collection export.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionExportService extends BaseExportService implements ExportCollectionUseCase {

    /**
     * The application's file repository.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * The JSON mapper.
     */
    @Getter
    private final JsonMapper jsonMapper;

    /**
     * Use case for use project dirs.
     */
    @Getter
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Use case for export menu.
     */
    private final ExportMenuUseCase exportMenuUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportCollection(CollectionExport collectionExport, Menu menu) {
        ExportContext exportContext = createExportContext(collectionExport.getId(), collectionExport.getExportConfiguration());
        exportContext.setId(collectionExport.getId());
        exportContext.setCoverPictureExtension(collectionExport.getCoverPictureExtension());

        prepareExport(exportContext);

        exportMainData(exportContext, ContentSource.COLLECTION, menu.getId(), collectionExport.getTitle(),
                collectionExport.getDescription(), collectionExport.getContent(), collectionExport.getContentAudio());

        exportConfigs(exportContext);

        exportMenuUseCase.exportMenu(exportContext, menu);

        if (StringUtils.hasText(collectionExport.getCoverPictureExtension())) {
            Path coverPictureFile = useProjectDirsUseCase.getExportsDir()
                    .resolve(collectionExport.getId() + "." + collectionExport.getCoverPictureExtension());
            if (fileRepository.exists(coverPictureFile)) {
                fileRepository.copy(coverPictureFile, exportContext.getExportDir()
                        .resolve("cover-picture." + collectionExport.getCoverPictureExtension()), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        copyContentAudioFiles(collectionExport, exportContext);

        cleanupExport(exportContext);

        return exportContext.getExportFile();
    }

    /**
     * Copies content audio files into the export directory.
     *
     * @param collectionExport The collection export.
     * @param exportContext     The export context.
     */
    private void copyContentAudioFiles(CollectionExport collectionExport, ExportContext exportContext) {
        TranslatableString contentAudio = collectionExport.getContentAudio();
        if (contentAudio == null) {
            return;
        }
        if (StringUtils.hasText(contentAudio.getValue())) {
            copyAudioFile(contentAudio.getValue(), exportContext);
        }
        contentAudio.getTranslations().values().forEach(filename -> {
            if (StringUtils.hasText(filename)) {
                copyAudioFile(filename, exportContext);
            }
        });
    }

    /**
     * Copies a single audio file into the export directory.
     *
     * @param filename      The audio filename.
     * @param exportContext The export context.
     */
    private void copyAudioFile(String filename, ExportContext exportContext) {
        Path audioFile = useProjectDirsUseCase.getExportsDir().resolve(filename);
        if (fileRepository.exists(audioFile)) {
            fileRepository.copy(audioFile, exportContext.getExportDir().resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
