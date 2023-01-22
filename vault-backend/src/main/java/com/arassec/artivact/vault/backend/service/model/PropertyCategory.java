package com.arassec.artivact.vault.backend.service.model;

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
public class PropertyCategory extends TranslatableItem {

    private List<Property> properties = new LinkedList<>();

}
