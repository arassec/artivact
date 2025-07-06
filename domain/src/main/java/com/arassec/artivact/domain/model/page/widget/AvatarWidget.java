package com.arassec.artivact.domain.model.page.widget;

import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.page.FileProcessingOperation;
import com.arassec.artivact.domain.model.page.FileProcessingWidget;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.WidgetType;
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
    public void processFile(String filename, FileProcessingOperation operation) {
        if (FileProcessingOperation.ADD.equals(operation)) {
            this.avatarImage = filename;
        } else if (FileProcessingOperation.REMOVE.equals(operation)) {
            this.avatarImage = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> usedFiles() {
        if (avatarImage != null) {
            return List.of(avatarImage);
        }
        return List.of();
    }

}
