package com.arassec.artivact.application.service.menu;

import com.arassec.artivact.application.port.in.page.CreatePageUseCase;
import com.arassec.artivact.application.port.in.page.DeletePageUseCase;
import com.arassec.artivact.application.port.in.page.UpdatePageAliasUseCase;
import com.arassec.artivact.application.port.out.repository.MenuRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageMenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private UpdatePageAliasUseCase updatePageAliasUseCase;

    @Mock
    private CreatePageUseCase createPageUseCase;

    @Mock
    private DeletePageUseCase deletePageUseCase;

    @InjectMocks
    private ManageMenuService manageMenuService;

    private List<Menu> menus;

    @BeforeEach
    void setup() {
        menus = new LinkedList<>();
        lenient().when(menuRepository.load()).thenReturn(menus);
    }

    @Test
    void testLoadTranslatedRestrictedMenus() {
        menus.add(new Menu());
        assertThat(manageMenuService.loadTranslatedRestrictedMenus()).hasSize(1);
    }

    @Test
    void testSaveMenusUpdatesAndSaves() {
        Menu menu = new Menu();
        menu.setId("menu1");
        menu.setTargetPageId("page1");
        menu.setTargetPageAlias("alias1");
        menu.setMenuEntries(List.of(new Menu()));

        List<Menu> result = manageMenuService.saveMenus(List.of(menu));

        verify(menuRepository).save(any(Menu.class), anyInt());
        verify(updatePageAliasUseCase).updatePageAlias("page1", "alias1");
        assertThat(result).isNotNull();
    }

    @Test
    void testSaveMenuNullReturnsExisting() {
        menus.add(new Menu());
        List<Menu> result = manageMenuService.saveMenu(null);
        assertThat(result).hasSize(1);
    }

    @Test
    void testSaveMenuWithoutTitleThrows() {
        Menu menu = new Menu();
        assertThatThrownBy(() -> manageMenuService.saveMenu(menu))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Menu title required!");
    }

    @Test
    void testSaveMenuWithInvalidSubMenuThrows() {
        Menu menu = new Menu();
        menu.setValue("Main menu");
        Menu invalidSub = new Menu();
        invalidSub.setValue(null);
        menu.setMenuEntries(List.of(invalidSub));

        assertThatThrownBy(() -> manageMenuService.saveMenu(menu))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Sub-Menu title required!");
    }

    @Test
    void testSaveMenuAddsNewMenu() {
        Menu menu = new Menu();
        menu.setId("menu1");
        menu.setValue("Menu title");

        Menu submenu = new Menu();
        submenu.setValue("Title");
        menu.getMenuEntries().add(submenu);

        Page createdPage = new Page();
        createdPage.setId("page1");
        createdPage.setAlias("alias");
        when(createPageUseCase.createPage(any())).thenReturn(createdPage);

        manageMenuService.saveMenu(menu);

        verify(menuRepository).save(any(Menu.class), anyInt());
        assertThat(menus).contains(menu);
    }

    @Test
    void testSaveMenuUpdatesExistingMenu() {
        Menu existing = new Menu();
        existing.setId("menu1");
        existing.setValue("Old");

        menus.add(existing);

        Menu updated = new Menu();
        updated.setId("menu1");
        updated.setValue("New Title");

        manageMenuService.saveMenu(updated);

        assertThat(menus.getFirst().getValue()).isEqualTo("New Title");
    }

    @Test
    void testDeleteMenuWithEmptyIdReturnsExisting() {
        menus.add(new Menu());
        List<Menu> result = manageMenuService.deleteMenu("");
        assertThat(result).hasSize(1);
    }

    @Test
    void testDeleteMenuRemovesTargetMenu() {
        Menu menu = new Menu();
        menu.setId("menu1");
        menu.setTargetPageId("page1");
        menus.add(menu);

        manageMenuService.deleteMenu("menu1");

        verify(deletePageUseCase).deletePage("page1");
        verify(menuRepository).deleteAll();
    }

    @Test
    void testAddPageToMenuWithEmptyIdReturnsExisting() {
        menus.add(new Menu());
        List<Menu> result = manageMenuService.addPageToMenu("");
        assertThat(result).hasSize(1);
    }

    @Test
    void testAddPageToMenuAddsPage() {
        Menu menu = new Menu();
        menu.setId("menu1");
        menus.add(menu);

        Page createdPage = new Page();
        createdPage.setId("page1");
        createdPage.setAlias("alias1");
        when(createPageUseCase.createPage(any())).thenReturn(createdPage);

        manageMenuService.addPageToMenu("menu1");

        verify(menuRepository).save(any(Menu.class), anyInt());
        assertThat(menu.getTargetPageId()).isEqualTo("page1");
    }

    @Test
    void testLoadMenuFindsMenu() {
        Menu menu = new Menu();
        menu.setId("menu1");
        menus.add(menu);

        Menu result = manageMenuService.loadMenu("menu1");
        assertThat(result).isEqualTo(menu);
    }

    @Test
    void testLoadMenuThrowsIfNotFound() {
        assertThatThrownBy(() -> manageMenuService.loadMenu("unknown"))
                .isInstanceOf(Exception.class);
    }

}
