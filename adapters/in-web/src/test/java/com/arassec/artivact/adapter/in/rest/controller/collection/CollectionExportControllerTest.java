package com.arassec.artivact.adapter.in.rest.controller.collection;

import com.arassec.artivact.application.port.in.collection.*;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link CollectionExportController}.
 */
@ExtendWith(MockitoExtension.class)
class CollectionExportControllerTest {

    /**
     * Controller under test.
     */
    @InjectMocks
    private CollectionExportController collectionExportController;

    /**
     * Use case for project directory handling.
     */
    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Repository for file access.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * Use case to import collections.
     */
    @Mock
    private ImportCollectionUseCase importCollectionUseCase;

    /**
     * Use case to load a collection export.
     */
    @Mock
    private LoadCollectionExportUseCase loadCollectionExportUseCase;

    /**
     * Use case to save collection exports.
     */
    @Mock
    private SaveCollectionExportUseCase saveCollectionExportUseCase;

    /**
     * Use case to delete collection exports.
     */
    @Mock
    private DeleteCollectionExportUseCase deleteCollectionExportUseCase;

    /**
     * Use case to build a collection export file.
     */
    @Mock
    private BuildCollectionExportFileUseCase buildCollectionExportFileUseCase;

    /**
     * Use case to read infos from a collection export file.
     */
    @Mock
    private ReadCollectionExportFileUseCase readCollectionExportFileUseCase;

    /**
     * Use case to save a cover picture of a collection export.
     */
    @Mock
    private SaveCollectionExportCoverPictureUseCase saveCollectionExportCoverPictureUseCase;

    /**
     * Use case to load a collection export's cover picture.
     */
    @Mock
    private LoadCollectionExportCoverPictureUseCase loadCollectionExportCoverPictureUseCase;

    /**
     * Use case do delete a collection export's cover picture.
     */
    @Mock
    private DeleteCollectionExportCoverPictureUseCase deleteCollectionExportCoverPictureUseCase;

    /**
     * Use case to create collection export infos.
     */
    @Mock
    private CreateCollectionExportInfosUseCase createCollectionExportInfosUseCase;

    /**
     * Tests loading collection exports.
     */
    @Test
    void testLoadCollectionExports() {
        List<CollectionExport> collectionExports = List.of();
        when(loadCollectionExportUseCase.loadAll()).thenReturn(collectionExports);
        assertThat(collectionExportController.loadCollectionExports()).isEqualTo(collectionExports);
    }

    /**
     * Tests saving a collection export.
     */
    @Test
    void testSave() {
        List<CollectionExport> collectionExports = List.of();
        when(loadCollectionExportUseCase.loadAll()).thenReturn(collectionExports);

        CollectionExport collectionExport = new CollectionExport();
        assertThat(collectionExportController.save(collectionExport)).isEqualTo(collectionExports);

        verify(saveCollectionExportUseCase).save(collectionExport);
    }

    /**
     * Tests saving the collection export sort order.
     */
    @Test
    void testSaveSortOrder() {
        List<CollectionExport> collectionExports = List.of();
        when(loadCollectionExportUseCase.loadAll()).thenReturn(collectionExports);

        assertThat(collectionExportController.saveSortOrder(collectionExports)).isEqualTo(collectionExports);

        verify(saveCollectionExportUseCase).saveSortOrder(collectionExports);
    }

    /**
     * Tests deleting a collection export.
     */
    @Test
    void testDelete() {
        List<CollectionExport> collectionExports = List.of();
        when(loadCollectionExportUseCase.loadAll()).thenReturn(collectionExports);

        assertThat(collectionExportController.delete("")).isEqualTo(collectionExports);
        verify(deleteCollectionExportUseCase, times(0)).delete(anyString());

        assertThat(collectionExportController.delete("collection-export-id")).isEqualTo(collectionExports);
        verify(deleteCollectionExportUseCase).delete("collection-export-id");
    }

    /**
     * Tests building a collection export.
     */
    @Test
    void testBuild() {
        collectionExportController.build("collection-export-id");
        verify(buildCollectionExportFileUseCase).buildExportFile("collection-export-id");
    }

}
