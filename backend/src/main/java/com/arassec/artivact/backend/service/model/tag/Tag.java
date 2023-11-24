package com.arassec.artivact.backend.service.model.tag;

import com.arassec.artivact.backend.service.model.BaseTranslatableRestrictedItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tag extends BaseTranslatableRestrictedItem {

    private String url;

    private boolean defaultTag;

}
