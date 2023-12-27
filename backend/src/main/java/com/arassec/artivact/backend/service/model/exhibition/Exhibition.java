package com.arassec.artivact.backend.service.model.exhibition;

import com.arassec.artivact.backend.service.model.BaseRestrictedItem;
import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.property.PropertyCategory;
import com.arassec.artivact.backend.service.model.tag.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Exhibition extends BaseRestrictedItem {

    private Integer version;

    private TranslatableString title;

    private TranslatableString description;

    private Instant lastModified;

    private List<String> referencedMenuIds = new LinkedList<>();

    private List<Topic> topics = new LinkedList<>();

    private List<PropertyCategory> propertyCategories = new LinkedList<>();

    private List<Tag> tags = new LinkedList<>();

}
