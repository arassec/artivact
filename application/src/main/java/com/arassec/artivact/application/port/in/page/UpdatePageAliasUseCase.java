package com.arassec.artivact.application.port.in.page;

/**
 * Use case for update page alias operations.
 */
public interface UpdatePageAliasUseCase {

    /**
     * Updates a page's alias and restrictions.
     *
     * @param pageId    The page's ID.
     * @param pageAlias The page's alias to use.
     */
    void updatePageAlias(String pageId, String pageAlias);

}
