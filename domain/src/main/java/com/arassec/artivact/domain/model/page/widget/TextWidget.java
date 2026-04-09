package com.arassec.artivact.domain.model.page.widget;

import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.page.ContentAudioProvider;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

/**
 * Displays a central text with an optional heading.
 */
@Getter
@Setter
public class TextWidget extends Widget implements ContentAudioProvider {

    /**
     * The heading.
     */
    private TranslatableString heading;

    /**
     * The text content.
     */
    private TranslatableString content;

    /**
     * The filename of the audio version of the content, per locale.
     */
    private TranslatableString contentAudio;

    /**
     * Creates a new widget.
     */
    public TextWidget() {
        super(WidgetType.TEXT);
    }

}
