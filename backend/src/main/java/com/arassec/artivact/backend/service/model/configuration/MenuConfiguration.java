package com.arassec.artivact.backend.service.model.configuration;

import com.arassec.artivact.backend.service.model.menu.Menu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of the application's menu.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuConfiguration {

    /**
     * Main menu configuration.
     */
    private List<Menu> menus = new LinkedList<>();

}
