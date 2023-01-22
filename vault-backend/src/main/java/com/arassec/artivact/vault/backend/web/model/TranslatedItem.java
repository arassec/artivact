package com.arassec.artivact.vault.backend.web.model;

import com.arassec.artivact.vault.backend.service.model.TranslatableItem;
import lombok.Getter;
import lombok.Setter;

public class TranslatedItem extends TranslatableItem {

    @Setter
    @Getter
    private String translatedValue;

}
