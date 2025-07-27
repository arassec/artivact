package com.arassec.artivact.adapter.out.database.jdbc.springdata.repository;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.ItemEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring-Data repository for {@link ItemEntity}s.
 */
public interface ItemEntityRepository extends PagingAndSortingRepository<ItemEntity, String>, CrudRepository<ItemEntity, String> {

    /**
     * Loads IDs of items that were modified since the last upload to a remote instance.
     *
     * @return List of IDs for syncing.
     */
    @Query(value = "SELECT id FROM av_item WHERE version != sync_version OR sync_version IS NULL ORDER BY id LIMIT :limit", nativeQuery = true)
    List<String> findItemIdsForRemoteExport(@Param("limit") int limit);

}
