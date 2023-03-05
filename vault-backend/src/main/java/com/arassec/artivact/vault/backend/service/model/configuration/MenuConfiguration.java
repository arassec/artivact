package com.arassec.artivact.vault.backend.service.model.configuration;

import com.arassec.artivact.vault.backend.service.model.Menu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuConfiguration {

    private List<Menu> menus = new LinkedList<>();

}
