package com.arassec.artivact.backend.service.model.export.avexhibition;

import lombok.Getter;

import java.util.Map;

@Getter
public class TranslatableText {

    private final String value;

    private final Map<String, String> translations;

    public TranslatableText(String value, Map<String, String> translations) {
        this.value = value;
        this.translations = translations;
    }

}
