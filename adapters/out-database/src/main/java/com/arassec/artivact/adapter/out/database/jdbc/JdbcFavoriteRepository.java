package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.FavoriteEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.FavoriteEntityRepository;
import com.arassec.artivact.application.port.out.repository.FavoriteRepository;
import com.arassec.artivact.domain.model.favorite.Favorite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link FavoriteRepository} implementation that uses JDBC for data access.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcFavoriteRepository implements FavoriteRepository {

    private final FavoriteEntityRepository favoriteEntityRepository;

    @Override
    public void save(Favorite favorite) {
        FavoriteEntity entity = new FavoriteEntity();
        entity.setUsername(favorite.getUsername());
        entity.setItemId(favorite.getItemId());
        entity.setCreatedAt(favorite.getCreatedAt());
        favoriteEntityRepository.save(entity);
    }

    @Override
    public void delete(String username, String itemId) {
        FavoriteEntity.FavoriteId id = new FavoriteEntity.FavoriteId(username, itemId);
        favoriteEntityRepository.deleteById(id);
    }

    @Override
    public boolean isFavorite(String username, String itemId) {
        return favoriteEntityRepository.existsByUsernameAndItemId(username, itemId);
    }

    @Override
    public List<Favorite> findByUsername(String username) {
        return favoriteEntityRepository.findByUsernameOrderByCreatedAtAsc(username).stream()
                .map(this::mapEntity)
                .toList();
    }

    @Override
    public void deleteByItemId(String itemId) {
        favoriteEntityRepository.deleteByItemId(itemId);
    }

    private Favorite mapEntity(FavoriteEntity entity) {
        return new Favorite(entity.getUsername(), entity.getItemId(), entity.getCreatedAt());
    }

}
