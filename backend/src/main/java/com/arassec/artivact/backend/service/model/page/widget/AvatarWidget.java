package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.page.FileProcessingWidget;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvatarWidget extends Widget implements FileProcessingWidget {

    private String avatarImage;

    private TranslatableString avatarSubtext;

    public AvatarWidget() {
        super(WidgetType.AVATAR);
    }

    @Override
    public void processFile(String fileName) {
        this.avatarImage = fileName;
    }

}
