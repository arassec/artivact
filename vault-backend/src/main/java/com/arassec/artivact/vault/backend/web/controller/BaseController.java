package com.arassec.artivact.vault.backend.web.controller;

import com.arassec.artivact.vault.backend.core.Roles;
import com.arassec.artivact.vault.backend.service.model.VaultArtivact;
import com.arassec.artivact.vault.backend.service.model.TranslatableItem;
import com.arassec.artivact.vault.backend.service.model.TranslatedItem;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public abstract class BaseController {

    protected String createMainImageUrl(VaultArtivact vaultArtivact) {
        if (!vaultArtivact.getMediaContent().getImages().isEmpty()) {
            return createImageUrl(vaultArtivact.getId(), vaultArtivact.getMediaContent().getImages().get(0));
        }
        return null;
    }

    protected String createImageUrl(String artivactId, String fileName) {
        return createUrl(artivactId, fileName, "image");
    }

    protected String createModelUrl(String artivactId, String fileName) {
        return createUrl(artivactId, fileName, "model");
    }

    protected String createUrl(String artivactId, String fileName, String fileType) {
        return "/api/artivact/media/" + artivactId + "/" + fileType + "/" + fileName;
    }

    protected TranslatedItem translateItem(TranslatableItem translatableItem, Locale locale) {
        TranslatedItem result = new TranslatedItem();
        if (translatableItem == null) {
            result.setValue("");
            result.setTranslations(new HashMap<>());
            result.setRestrictions(new LinkedList<>());
            result.setTranslatedValue("");
        } else {
            result.setId(translatableItem.getId());
            result.setValue(translatableItem.getValue());
            result.setTranslations(translatableItem.getTranslations());
            result.setRestrictions(translatableItem.getRestrictions());
            result.setTranslatedValue(translatableItem.getTranslatedValue(locale.toString()));
        }
        return result;
    }

    protected TranslatableItem removeTranslation(TranslatedItem translatedItem) {
        TranslatableItem result = new TranslatableItem();
        if (translatedItem == null) {
            result.setValue("");
            result.setTranslations(new HashMap<>());
            result.setRestrictions(new LinkedList<>());
        } else {
            result.setId(translatedItem.getId());
            result.setValue(translatedItem.getValue());
            result.setTranslations(translatedItem.getTranslations());
            result.setRestrictions(translatedItem.getRestrictions());
        }
        return result;
    }

    protected List<String> getRoles(Authentication authentication) {
        if (authentication == null) {
            return List.of();
        }
        if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return List.of();
        }
        if (userDetails.getAuthorities() == null) {
            return List.of();
        }
        return userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }

    protected boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return getRoles(authentication).contains(Roles.ROLE_ADMIN);
    }

    protected boolean isUser(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return getRoles(authentication).contains(Roles.ROLE_USER);
    }

    protected boolean isAdminOrUser(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return isAdmin(authentication) || isUser(authentication);
    }
}
