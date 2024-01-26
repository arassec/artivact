package com.arassec.artivact.backend.persistence;

import com.arassec.artivact.backend.persistence.model.AccountEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountEntityRepository extends CrudRepository<AccountEntity, Integer> {

    Optional<AccountEntity> findByUsername(String username);

    Optional<AccountEntity> findByApiToken(String apiToken);

}
