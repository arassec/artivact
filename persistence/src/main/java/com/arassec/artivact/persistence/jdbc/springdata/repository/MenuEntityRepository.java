package com.arassec.artivact.persistence.jdbc.springdata.repository;

import com.arassec.artivact.persistence.jdbc.springdata.entity.MenuEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

/**
 * Spring-Data repository for {@link MenuEntity}s.
 */
@SuppressWarnings("SqlDialectInspection")
public interface MenuEntityRepository extends CrudRepository<MenuEntity, String> {

    /**
     * Deletes all menus that are not listed by their ID.
     *
     * @param ids The list of IDs of menus to keep.
     */
    @Modifying
    @Query(value = "DELETE FROM av_menu WHERE id NOT IN :ids", nativeQuery = true)
    void deleteWhereIdNotIn(Set<String> ids);

}
