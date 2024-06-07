package com.arassec.artivact.backend.persistence;

import com.arassec.artivact.backend.persistence.model.ItemEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

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
    @Query("SELECT id FROM ItemEntity WHERE version != syncVersion")
    List<String> findItemIdsForRemoteExport();

}
