package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.page.FileProcessingWidget;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageTitleWidget extends Widget implements FileProcessingWidget {

    private TranslatableString title;

    private String backgroundImage;

    public PageTitleWidget() {
        super(WidgetType.PAGE_TITLE);
    }

    @Override
    public void processFile(String fileName) {
        this.backgroundImage = fileName;
    }

}
