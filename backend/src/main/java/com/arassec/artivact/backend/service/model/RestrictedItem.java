package com.arassec.artivact.backend.service.model;

import java.util.Collection;

public interface RestrictedItem {

    boolean isAllowed(Collection<String> roles);

}
