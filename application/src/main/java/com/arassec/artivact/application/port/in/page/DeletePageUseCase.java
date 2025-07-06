package com.arassec.artivact.application.port.in.page;

public interface DeletePageUseCase {

    /**
     * Deletes the page with the given ID.
     *
     * @param pageIdOrAlias The page's ID or alias.
     */
    void deletePage(String pageIdOrAlias);

}
