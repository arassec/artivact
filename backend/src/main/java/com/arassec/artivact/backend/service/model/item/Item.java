package com.arassec.artivact.backend.service.model.item;

import com.arassec.artivact.backend.service.model.BaseRestrictedItem;
import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.tag.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class Item extends BaseRestrictedItem {

    private Integer version;

    private TranslatableString title;

    private TranslatableString description;

    private Map<String, String> properties = new HashMap<>();

    private MediaContent mediaContent;

    private MediaCreationContent mediaCreationContent;

    private List<Tag> tags = new LinkedList<>();

}
