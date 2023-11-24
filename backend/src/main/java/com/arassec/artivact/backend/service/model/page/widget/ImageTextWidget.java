package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.page.FileProcessingWidget;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageTextWidget extends Widget implements FileProcessingWidget {

    private String image;

    private TranslatableString text;

    public ImageTextWidget() {
        super(WidgetType.IMAGE_TEXT);
    }

    @Override
    public void processFile(String fileName) {
        this.image = fileName;
    }

}
