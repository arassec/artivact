package com.arassec.artivact.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains the application profiles and whether they are active or not.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profiles {

    /**
     * The desktop profile.
     */
    private boolean desktop;

    /**
     * The E2E-Testing profile.
     */
    private boolean e2e;

}
