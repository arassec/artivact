package com.arassec.artivact.persistence.jdbc;

import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.core.repository.CollectionExportRepository;
import com.arassec.artivact.persistence.jdbc.springdata.entity.CollectionExportEntity;
import com.arassec.artivact.persistence.jdbc.springdata.repository.CollectionExportEntityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link CollectionExportRepository} implementation that uses JDBC.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcCollectionExportRepository extends BaseJdbcRepository implements CollectionExportRepository {

    /**
     * Spring-Data's repository for {@link CollectionExportEntity}s.
     */
    private final CollectionExportEntityRepository collectionExportEntityRepository;

    /**
     * The systems ObjectMapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    @Override
    public Optional<CollectionExport> findById(String id) {
        Optional<CollectionExportEntity> collectionExportEntityOptional = collectionExportEntityRepository.findById(id);
        return collectionExportEntityOptional
                .map(collectionExportEntity -> fromJson(collectionExportEntity.getContentJson(), CollectionExport.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CollectionExport> findAll() {
        List<CollectionExportEntity> collectionExportEntities = new LinkedList<>();
        collectionExportEntityRepository.findAll().forEach(collectionExportEntities::add);

        collectionExportEntities.sort(Comparator.comparing(CollectionExportEntity::getSortOrder));

        return collectionExportEntities.stream()
                .map(contentExportEntity -> fromJson(contentExportEntity.getContentJson(), CollectionExport.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(CollectionExport contentExport) {
        Optional<CollectionExportEntity> contentExportEntityOptional = collectionExportEntityRepository.findById(contentExport.getId());

        CollectionExportEntity contentExportEntity = contentExportEntityOptional.orElse(null);
        if (contentExportEntity == null) {
            contentExportEntity = new CollectionExportEntity();
            contentExportEntity.setId(contentExport.getId());
            contentExportEntity.setSortOrder(collectionExportEntityRepository.findMaxSortOrder().orElse(-1) + 1);
        }

        contentExportEntity.setContentJson(toJson(contentExport));

        collectionExportEntityRepository.save(contentExportEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String id) {
        collectionExportEntityRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSortOrder(List<CollectionExport> collectionExports) {
        for (int i=0; i < collectionExports.size(); i++) {
            CollectionExport collectionExport = collectionExports.get(i);
            CollectionExportEntity collectionExportEntity = collectionExportEntityRepository.findById(collectionExport.getId()).orElseThrow();
            collectionExportEntity.setSortOrder(i);
            collectionExportEntityRepository.save(collectionExportEntity);
        }
    }

}
