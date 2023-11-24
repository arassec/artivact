package com.arassec.artivact.backend.service.model.menu;

import com.arassec.artivact.backend.service.model.BaseTranslatableRestrictedItem;
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
public class Menu extends BaseTranslatableRestrictedItem {

    private String parentId;

    private List<Menu> menuEntries = new LinkedList<>();

    private String targetPageId;

}
