package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.CollectionExportEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.CollectionExportEntityRepository;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcCollectionExportRepository}.
 */
@ExtendWith(MockitoExtension.class)
class JdbcCollectionExportRepositoryTest {

    /**
     * Repository under test.
     */
    @InjectMocks
    private JdbcCollectionExportRepository jdbcCollectionExportRepository;

    /**
     * Spring-Data's collection export repository mock.
     */
    @Mock
    private CollectionExportEntityRepository collectionExportEntityRepository;

    /**
     * ObjectMapper mock.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Tests finding a collection export by its ID.
     */
    @Test
    @SneakyThrows
    void testFindById() {
        CollectionExportEntity entity = new CollectionExportEntity();
        entity.setContentJson("{content-json}");
        when(collectionExportEntityRepository.findById("id")).thenReturn(Optional.of(entity));

        CollectionExport collectionExport = new CollectionExport();
        when(objectMapper.readValue("{content-json}", CollectionExport.class)).thenReturn(collectionExport);

        CollectionExport result = jdbcCollectionExportRepository.findById("id").orElseThrow(AssertionError::new);

        assertThat(result).isEqualTo(collectionExport);
    }

    /**
     * Tests finding all collection exports.
     */
    @Test
    @SneakyThrows
    void testFindAll() {
        CollectionExportEntity entityOne = new CollectionExportEntity();
        entityOne.setContentJson("{content-json-one}");
        entityOne.setSortOrder(42);

        CollectionExportEntity entityTwo = new CollectionExportEntity();
        entityTwo.setContentJson("{content-json-two}");
        entityTwo.setSortOrder(23);

        when(collectionExportEntityRepository.findAll()).thenReturn(List.of(entityOne, entityTwo));

        CollectionExport collectionExportOne = new CollectionExport();
        when(objectMapper.readValue("{content-json-one}", CollectionExport.class)).thenReturn(collectionExportOne);
        CollectionExport collectionExportTwo = new CollectionExport();
        when(objectMapper.readValue("{content-json-two}", CollectionExport.class)).thenReturn(collectionExportTwo);

        List<CollectionExport> result = jdbcCollectionExportRepository.findAll();

        assertThat(result).hasSize(2);
        // Sort order must be applied!
        assertThat(result.get(0)).isEqualTo(collectionExportTwo);
        assertThat(result.get(1)).isEqualTo(collectionExportOne);
    }

    /**
     * Tests saving a new collection export configuration.
     */
    @Test
    @SneakyThrows
    void testSaveNew() {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setId("id");

        when(collectionExportEntityRepository.findById(anyString())).thenReturn(Optional.empty());

        when(collectionExportEntityRepository.findMaxSortOrder()).thenReturn(Optional.of(23));

        when(objectMapper.writeValueAsString(any(CollectionExport.class))).thenReturn("{content-json}");

        ArgumentCaptor<CollectionExportEntity> argCap = ArgumentCaptor.forClass(CollectionExportEntity.class);

        jdbcCollectionExportRepository.save(collectionExport);

        verify(collectionExportEntityRepository).save(argCap.capture());

        assertThat(argCap.getValue().getContentJson()).isEqualTo("{content-json}");
        assertThat(argCap.getValue().getSortOrder()).isEqualTo(24);
    }

    /**
     * Tests deleting a collection export.
     */
    @Test
    void testDelete() {
        jdbcCollectionExportRepository.delete("id");
        verify(collectionExportEntityRepository).deleteById("id");
    }

    /**
     * Tests saving the collection export sort order.
     */
    @Test
    void testSaveSortOrder() {
        CollectionExportEntity entityOne = new CollectionExportEntity();
        entityOne.setContentJson("{content-json-one}");
        entityOne.setSortOrder(42);
        when(collectionExportEntityRepository.findById("one")).thenReturn(Optional.of(entityOne));

        CollectionExportEntity entityTwo = new CollectionExportEntity();
        entityTwo.setContentJson("{content-json-two}");
        entityTwo.setSortOrder(23);
        when(collectionExportEntityRepository.findById("two")).thenReturn(Optional.of(entityTwo));

        CollectionExport one = new CollectionExport();
        one.setId("one");

        CollectionExport two = new CollectionExport();
        two.setId("two");

        List<CollectionExport> collectionExports = List.of(one, two);

        ArgumentCaptor<CollectionExportEntity> argCap = ArgumentCaptor.forClass(CollectionExportEntity.class);

        jdbcCollectionExportRepository.saveSortOrder(collectionExports);

        verify(collectionExportEntityRepository, times(2)).save(argCap.capture());

        assertThat(argCap.getAllValues().get(0).getSortOrder()).isZero();
        assertThat(argCap.getAllValues().get(1).getSortOrder()).isEqualTo(1);
    }

}
