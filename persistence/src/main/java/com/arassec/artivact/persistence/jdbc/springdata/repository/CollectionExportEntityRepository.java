package com.arassec.artivact.persistence.jdbc.springdata.repository;

import com.arassec.artivact.persistence.jdbc.springdata.entity.CollectionExportEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Spring-Data repository for {@link CollectionExportEntity}s.
 */
@SuppressWarnings("SqlDialectInspection")
public interface CollectionExportEntityRepository extends CrudRepository<CollectionExportEntity, String> {

    /**
     * Returns the biggest sort-order number currently used.
     *
     * @return The biggest sort order number.
     */
    @Query(value = "SELECT MAX(sort_order) FROM av_collection_export", nativeQuery = true)
    Optional<Integer> findMaxSortOrder();

}
