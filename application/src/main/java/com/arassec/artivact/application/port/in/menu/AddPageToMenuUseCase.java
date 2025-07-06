package com.arassec.artivact.application.port.in.menu;

import com.arassec.artivact.domain.model.menu.Menu;

import java.util.List;

public interface AddPageToMenuUseCase {

    List<Menu> addPageToMenu(String menuId);

}
