package com.arassec.artivact.application.port.in.page;

import com.arassec.artivact.domain.model.page.Page;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.PageIdAndAlias;

import java.util.Optional;
import java.util.Set;

/**
 * Use case for load page content operations.
 */
public interface LoadPageContentUseCase {

    /**
     * Loads the content of the given page.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @param roles         The available roles.
     * @return The {@link PageContent} of the page.
     */
    PageContent loadPageContent(String pageIdOrAlias, Set<String> roles);

    /**
     * Loads the 'work-in-progress' content of the given page.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @param roles         The available roles.
     * @return The {@link PageContent} of the page.
     */
    PageContent loadTranslatedRestrictedWipPageContent(String pageIdOrAlias, Set<String> roles);

    /**
     * Loads the content of the given page and applies translations and restrictions.
     *
     * @param pageIdOrAlias The page's ID or the page's alias.
     * @param roles         The available roles.
     * @return The {@link PageContent} of the page.
     */
    PageContent loadTranslatedRestrictedPageContent(String pageIdOrAlias, Set<String> roles);

    /**
     * Loads the index page if available.
     *
     * @return The index {@link Page}.
     */
    Optional<PageIdAndAlias> loadIndexPageIdAndAlias();

}
