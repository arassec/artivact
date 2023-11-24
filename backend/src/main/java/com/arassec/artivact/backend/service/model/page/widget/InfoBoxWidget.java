package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfoBoxWidget  extends Widget {

    private TranslatableString heading;

    private TranslatableString content;

    private String boxType;

    public InfoBoxWidget() {
        super(WidgetType.INFO_BOX);
    }

}
