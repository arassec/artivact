package com.arassec.artivact.vault.backend.web.model;

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
public class TranslatedProperty extends TranslatedItem {

    private List<TranslatedItem> valueRange = new LinkedList<>();

}
