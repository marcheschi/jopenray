package com.medicalreports.profile;

import java.util.Arrays;
import java.util.List;

/**
 * General Practitioner (Medico di Base) medical profile.
 */
public class GeneralPractitionerProfile implements MedicalProfile {

    @Override
    public String getProfileId() {
        return "GP";
    }

    @Override
    public String getDisplayName() {
        return "Medico di Base";
    }

    @Override
    public String getTemplateName() {
        return "template_gp.odt";
    }

    @Override
    public List<String> getBookmarkNames() {
        return Arrays.asList(
            "BM_ANAMNESI",
            "BM_ESAME_OBIETTIVO",
            "BM_TERAPIA"
        );
    }

    @Override
    public String getDescription() {
        return "Profilo per medicina generale con anamnesi, esame obiettivo e terapia.";
    }
}
