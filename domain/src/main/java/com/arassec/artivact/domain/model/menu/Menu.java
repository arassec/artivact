package com.arassec.artivact.domain.model.menu;

import com.arassec.artivact.domain.model.BaseTranslatableRestrictedObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A menu.
 */
@Getter
@Setter
@NoArgsConstructor
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

    /**
     * Creates a new instance with the specified parameters.
     */
    @SuppressWarnings("java:S107") // This constructor is required as fallback for Jackson JSON deserialization.
    public Menu(String value, String translatedValue, Map<String, String> translations, String id, Set<String> restrictions, String parentId, List<Menu> menuEntries, String targetPageId, String targetPageAlias, boolean hidden, String external) {
        super(value, translatedValue, translations, id, restrictions);
        this.parentId = parentId;
        if (menuEntries != null) {
            this.menuEntries = menuEntries;
        }
        this.targetPageId = targetPageId;
        this.targetPageAlias = targetPageAlias;
        this.hidden = hidden;
        this.external = external;
    }

}
