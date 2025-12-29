package com.arassec.artivact.application.port.in.menu;

import com.arassec.artivact.domain.model.menu.Menu;

import java.util.List;

/**
 * Use case for add page to menu operations.
 */
public interface AddPageToMenuUseCase {

    List<Menu> addPageToMenu(String menuId);

}
