package com.arassec.artivact.backend.service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class BaseTranslatableRestrictedItem extends TranslatableString implements IdentifiedItem, RestrictedItem {

    private String id;

    private Set<String> restrictions = new HashSet<>();

    @Override
    public boolean isAllowed(Collection<String> roles) {
        if (restrictions.isEmpty()) {
            return true; // no restrictions
        }
        if (roles == null) {
            return false; // restrictions, but user has no roles
        }
        return restrictions.stream().anyMatch(roles::contains);
    }

}
