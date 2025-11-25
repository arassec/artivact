package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.MenuEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.MenuEntityRepository;
import com.arassec.artivact.application.port.out.repository.MenuRepository;
import com.arassec.artivact.domain.model.configuration.MenuConfiguration;
import com.arassec.artivact.domain.model.menu.Menu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.*;
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
    public MenuConfiguration load() {
        List<MenuEntity> sortedMenuEntities = new LinkedList<>();
        menuEntityRepository.findAll().forEach(sortedMenuEntities::add);
        sortedMenuEntities.sort(Comparator.comparing(MenuEntity::getSortOrder));

        MenuConfiguration menuConfiguration = new MenuConfiguration();

        menuConfiguration.setMenus(sortedMenuEntities.stream()
                .map(menuEntity -> fromJson(menuEntity.getContentJson(), Menu.class))
                .collect(Collectors.toList()));

        return menuConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(MenuConfiguration menuConfiguration) {
        Set<String> existingMenuIds = new HashSet<>();
        for (int i = 0; i < menuConfiguration.getMenus().size(); i++) {
            Menu menu = menuConfiguration.getMenus().get(i);
            existingMenuIds.add(menu.getId());

            Optional<MenuEntity> menuEntityOptional = menuEntityRepository.findById(menu.getId());
            MenuEntity menuEntity = menuEntityOptional.orElseGet(MenuEntity::new);

            menuEntity.setId(menu.getId());
            menuEntity.setSortOrder(i);
            menuEntity.setContentJson(toJson(menu));

            menuEntityRepository.save(menuEntity);
        }
        if (existingMenuIds.isEmpty()) {
            menuEntityRepository.deleteAll();
        } else {
            menuEntityRepository.deleteWhereIdNotIn(existingMenuIds);
        }
    }

}
