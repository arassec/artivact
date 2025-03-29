package com.arassec.artivact.core.model.menu;

import com.arassec.artivact.core.model.BaseTranslatableRestrictedObject;
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
     * An optional alias under which the page is accessible, too.
     */
    private String targetPageAlias;

    /**
     * If {@code true}, the menu is hidden, but the underlying page is still accessible with regard
     * to the configured restrictions. This enables pages to be accessible to everyone without the
     * need to login and without a visible menu.
     */
    private boolean hidden;

    /**
     * An optional URL to an external page that this menu entry will lead to.
     */
    private String external;

}
