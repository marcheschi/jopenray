package com.medicalreports.profile;

import java.util.Arrays;
import java.util.List;

/**
 * Dermatologist medical profile.
 */
public class DermatologistProfile implements MedicalProfile {

    @Override
    public String getProfileId() {
        return "DERMATOLOGO";
    }

    @Override
    public String getDisplayName() {
        return "Dermatologo";
    }

    @Override
    public String getTemplateName() {
        return "template_dermatologo.odt";
    }

    @Override
    public List<String> getBookmarkNames() {
        return Arrays.asList(
            "BM_ANAMNESI",
            "BM_MAPPATURA_LESIONI",
            "BM_REPERTI_DERMOOSCOPIA",
            "BM_DIAGNOSI",
            "BM_TERAPIA"
        );
    }

    @Override
    public String getDescription() {
        return "Profilo per dermatologia con mappatura lesioni e reperti dermoscopici.";
    }
}
