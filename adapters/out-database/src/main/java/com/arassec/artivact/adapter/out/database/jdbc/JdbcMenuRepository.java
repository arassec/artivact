package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.MenuEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.MenuEntityRepository;
import com.arassec.artivact.application.infrastructure.aspect.PersistEntityAsJson;
import com.arassec.artivact.application.port.out.repository.MenuRepository;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.misc.ExchangeDefinitions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link MenuRepository} implementation that uses JDBC.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcMenuRepository extends BaseJdbcRepository implements MenuRepository {

    /**
     * Spring-Data's repository for {@link MenuEntity}s.
     */
    private final MenuEntityRepository menuEntityRepository;

    /**
     * The systems ObjectMapper.
     */
    @Getter
    private final JsonMapper jsonMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("java:S6204") // The result list of menus needs to be modifiable!
    public List<Menu> load() {
        List<MenuEntity> sortedMenuEntities = new LinkedList<>();
        menuEntityRepository.findAll().forEach(sortedMenuEntities::add);
        sortedMenuEntities.sort(Comparator.comparing(MenuEntity::getSortOrder));

        return sortedMenuEntities.stream()
                .map(menuEntity -> fromJson(menuEntity.getContentJson(), Menu.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PersistEntityAsJson(entityDir = DirectoryDefinitions.MENUS_DIR, entityType = Menu.class, filename = ExchangeDefinitions.MENU_EXCHANGE_FILENAME_JSON)
    public void save(Menu menu) {
        Optional<MenuEntity> menuEntityOptional = menuEntityRepository.findById(menu.getId());
        MenuEntity menuEntity = menuEntityOptional.orElseGet(MenuEntity::new);

        menuEntity.setId(menu.getId());
        menuEntity.setSortOrder(menu.getIndex());
        menuEntity.setContentJson(toJson(menu));

        menuEntityRepository.save(menuEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PersistEntityAsJson(entityDir = DirectoryDefinitions.MENUS_DIR, entityType = Menu.class, delete = true, filename = ExchangeDefinitions.MENU_EXCHANGE_FILENAME_JSON)
    public void deleteById(String menuId) {
        menuEntityRepository.deleteById(menuId);
    }

}
