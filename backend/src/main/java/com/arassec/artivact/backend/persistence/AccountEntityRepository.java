package com.arassec.artivact.backend.persistence;

import com.arassec.artivact.backend.persistence.model.AccountEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Spring-Data repository for {@link AccountEntity}s.
 */
public interface AccountEntityRepository extends CrudRepository<AccountEntity, Integer> {

    /**
     * Finds an account by username.
     *
     * @param username The username.
     * @return An account with that username.
     */
    Optional<AccountEntity> findByUsername(String username);

    /**
     * Finds an account by an API token.
     *
     * @param apiToken The API token.
     * @return An account with that token.
     */
    Optional<AccountEntity> findByApiToken(String apiToken);

}
