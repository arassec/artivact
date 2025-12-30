package com.arassec.artivact.application.port.in.page;

/**
 * Use case for delete page operations.
 */
public interface DeletePageUseCase {

    /**
     * Deletes the page with the given ID.
     *
     * @param pageIdOrAlias The page's ID or alias.
     */
    void deletePage(String pageIdOrAlias);

}
