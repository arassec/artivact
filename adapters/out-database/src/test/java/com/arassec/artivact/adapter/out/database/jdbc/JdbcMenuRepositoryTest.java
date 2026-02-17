package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.MenuEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.MenuEntityRepository;
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
     * Tests loading all menus.
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

        List<Menu> menus = jdbcMenuRepository.load();

        assertEquals(List.of(firstMenu, secondMenu), menus);
    }

    /**
     * Tests saving a single menu.
     */
    @Test
    void testSave() {
        Menu menu = new Menu();
        menu.setId("menu-id");
        menu.setIndex(0);

        MenuEntity existingEntity = new MenuEntity();
        when(menuEntityRepository.findById("menu-id")).thenReturn(Optional.of(existingEntity));

        jdbcMenuRepository.save(menu);

        ArgumentCaptor<MenuEntity> argCap = ArgumentCaptor.forClass(MenuEntity.class);
        verify(menuEntityRepository, times(1)).save(argCap.capture());

        assertThat(argCap.getValue().getSortOrder()).isZero();
        assertThat(argCap.getValue().getId()).isEqualTo("menu-id");
    }

    /**
     * Tests saving a new menu (not yet in database).
     */
    @Test
    void testSaveNewMenu() {
        Menu menu = new Menu();
        menu.setId("new-menu-id");
        menu.setIndex(1);

        when(menuEntityRepository.findById("new-menu-id")).thenReturn(Optional.empty());

        jdbcMenuRepository.save(menu);

        ArgumentCaptor<MenuEntity> argCap = ArgumentCaptor.forClass(MenuEntity.class);
        verify(menuEntityRepository, times(1)).save(argCap.capture());

        assertThat(argCap.getValue().getSortOrder()).isEqualTo(1);
        assertThat(argCap.getValue().getId()).isEqualTo("new-menu-id");
    }

    /**
     * Tests deleting a menu by its ID.
     */
    @Test
    void testDelete() {
        jdbcMenuRepository.delete("menu-id");
        verify(menuEntityRepository).deleteById("menu-id");
    }

}
