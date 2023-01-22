package com.arassec.artivact.vault.backend.persistence.repository;

import com.arassec.artivact.vault.backend.persistence.model.ArtivactEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;

public interface ArtivactEntityRepository extends PagingAndSortingRepository<ArtivactEntity, String>,
        CrudRepository<ArtivactEntity, String> {

    void deleteAllByScannedBefore(LocalDateTime scanTime);

}
