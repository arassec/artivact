package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.page.FileProcessingWidget;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The avatar widget, showing a central image with optional subtext.
 */
@Getter
@Setter
public class AvatarWidget extends Widget implements FileProcessingWidget {

    /**
     * The image.
     */
    private String avatarImage;

    /**
     * The subtext.
     */
    private TranslatableString avatarSubtext;

    /**
     * Creates an instance.
     */
    public AvatarWidget() {
        super(WidgetType.AVATAR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processFile(String filename) {
        this.avatarImage = filename;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> usedFiles() {
        return List.of(avatarImage);
    }

}
