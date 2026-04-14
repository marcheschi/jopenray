package com.medicalreports.profile;

import java.util.List;

/**
 * Interface for medical profile definitions.
 */
public interface MedicalProfile {

    /**
     * Returns the profile identifier (e.g., "GP", "DERMATOLOGO", "CARDIOLOGO").
     */
    String getProfileId();

    /**
     * Returns the display name in Italian.
     */
    String getDisplayName();

    /**
     * Returns the template filename (e.g., "template_gp.odt").
     */
    String getTemplateName();

    /**
     * Returns the list of bookmark names expected in the template.
     */
    List<String> getBookmarkNames();

    /**
     * Returns the description of the profile.
     */
    String getDescription();
}
