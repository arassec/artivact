package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.MenuEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.MenuEntityRepository;
import com.arassec.artivact.domain.model.configuration.MenuConfiguration;
import com.arassec.artivact.domain.model.menu.Menu;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcMenuRepository}.
 */
@ExtendWith(MockitoExtension.class)
class JdbcMenuRepositoryTest {

    /**
     * The repository under test.
     */
    @InjectMocks
    private JdbcMenuRepository jdbcMenuRepository;

    /**
     * Mock of Spring-Data's repository for {@link MenuEntity}s.
     */
    @Mock
    private MenuEntityRepository menuEntityRepository;

    /**
     * ObjectMapper mock.
     */
    @Mock
    private JsonMapper jsonMapper;

    /**
     * Tests loading the menu configuration.
     */
    @Test
    @SneakyThrows
    void testLoad() {
        MenuEntity second = new MenuEntity();
        second.setSortOrder(1);
        second.setContentJson("second");
        MenuEntity first = new MenuEntity();
        first.setSortOrder(0);
        first.setContentJson("first");

        Menu firstMenu = new Menu();
        when(jsonMapper.readValue("first", Menu.class)).thenReturn(firstMenu);
        Menu secondMenu = new Menu();
        when(jsonMapper.readValue("second", Menu.class)).thenReturn(secondMenu);

        when(menuEntityRepository.findAll()).thenReturn(List.of(second, first));

        MenuConfiguration menuConfiguration = jdbcMenuRepository.load();

        assertEquals(List.of(firstMenu, secondMenu), menuConfiguration.getMenus());
    }

    /**
     * Tests saving the menu configuration.
     */
    @Test
    void testSave() {
        Menu first = new Menu();
        first.setId("first-id");
        Menu second = new Menu();
        second.setId("second-id");

        MenuEntity firstEntity = new MenuEntity();
        when(menuEntityRepository.findById("first-id")).thenReturn(Optional.of(firstEntity));

        MenuConfiguration menuConfiguration = new MenuConfiguration();
        menuConfiguration.setMenus(List.of(first, second));

        jdbcMenuRepository.save(menuConfiguration);

        ArgumentCaptor<MenuEntity> argCap = ArgumentCaptor.forClass(MenuEntity.class);
        verify(menuEntityRepository, times(2)).save(argCap.capture());

        assertThat(argCap.getAllValues().get(0).getSortOrder()).isZero();
        assertThat(argCap.getAllValues().get(1).getSortOrder()).isEqualTo(1);

        verify(menuEntityRepository).deleteWhereIdNotIn(Set.of("first-id", "second-id"));
    }

}
