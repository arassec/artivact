package com.arassec.artivact.domain.model.page;

import com.arassec.artivact.domain.model.TranslatableString;

/**
 * Interface for widgets that provide content and content audio.
 */
public interface ContentAudioProvider {

    /**
     * Returns the content of the widget.
     *
     * @return The widget's content as {@link TranslatableString}.
     */
    TranslatableString getContent();

    /**
     * Returns the content audio of the widget.
     *
     * @return The widget's content audio as {@link TranslatableString}.
     */
    TranslatableString getContentAudio();

}
