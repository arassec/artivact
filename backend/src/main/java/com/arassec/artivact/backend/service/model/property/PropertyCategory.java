package com.arassec.artivact.backend.service.model.property;

import com.arassec.artivact.backend.service.model.BaseTranslatableRestrictedItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyCategory extends BaseTranslatableRestrictedItem {

    private List<Property> properties = new LinkedList<>();

}
