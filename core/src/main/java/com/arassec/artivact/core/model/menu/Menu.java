package com.arassec.artivact.core.model.menu;

import com.arassec.artivact.core.model.BaseTranslatableRestrictedObject;
import com.arassec.artivact.core.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * A menu.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Menu extends BaseTranslatableRestrictedObject {

    /**
     * Optional menu ID of a parent menu.
     */
    private String parentId;

    /**
     * Entries in this menu.
     */
    private List<Menu> menuEntries = new LinkedList<>();

    /**
     * The ID of the target page, if any.
     */
    private String targetPageId;

    /**
     * Title of an export based on this menu.
     */
    private TranslatableString exportTitle;

    /**
     * Description of an export based on this menu.
     */
    private TranslatableString exportDescription;

}
