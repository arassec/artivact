package com.arassec.artivact.vault.backend.persistence.repository;

import com.arassec.artivact.vault.backend.persistence.model.ArtivactEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ArtivactEntityRepository extends PagingAndSortingRepository<ArtivactEntity, String>,
        CrudRepository<ArtivactEntity, String> {

    void deleteAllByScannedBefore(LocalDateTime scanTime);

    List<ArtivactEntity> findByTitleJsonContainingIgnoreCase(String searchTerm);

    List<ArtivactEntity> findByDescriptionJsonContainingIgnoreCase(String searchTerm);

    List<ArtivactEntity> findByPropertiesJsonContainingIgnoreCase(String searchTerm);

    List<ArtivactEntity> findByTagsJsonContainingIgnoreCase(String searchTerm);

}
