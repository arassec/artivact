package com.arassec.artivact.backend.service.model;

import java.util.Locale;

public interface TranslatableItem {

    String translate(String locale);

    String translate(Locale locale);

}
