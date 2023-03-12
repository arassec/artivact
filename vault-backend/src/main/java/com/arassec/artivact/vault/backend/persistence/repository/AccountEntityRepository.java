package com.arassec.artivact.vault.backend.persistence.repository;

import com.arassec.artivact.vault.backend.persistence.model.AccountEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountEntityRepository extends CrudRepository<AccountEntity, Integer> {

    Optional<AccountEntity> findByUsername(String username);

}
