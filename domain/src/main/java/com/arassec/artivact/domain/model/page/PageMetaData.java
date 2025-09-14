package com.arassec.artivact.domain.model.page;

import com.arassec.artivact.domain.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata of a page.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageMetaData {

    /**
     * The page's title.
     */
    private TranslatableString title;

    /**
     * Description of a page.
     */
    private TranslatableString description;

    /**
     * Author of a page.
     */
    private String author;

    /**
     * Comma-separated list of keywords of a page.
     */
    private TranslatableString keywords;

}
