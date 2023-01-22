package com.arassec.artivact.vault.backend.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranslatableItem {

    private String id;

    private String value;

    private List<String> restrictions = new LinkedList<>();

    private Map<String, String> translations = new HashMap<>();

    public String getTranslatedValue(String locale) {
        if (translations.containsKey(locale)) {
            return translations.get(locale);
        }
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            if (locale.startsWith(entry.getKey() + "_")) {
                return entry.getValue();
            }
        }
        return value;
    }

}
