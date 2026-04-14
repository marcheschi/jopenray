package com.medicalreports.profile;

import java.util.Arrays;
import java.util.List;

/**
 * Cardiologist medical profile.
 */
public class CardiologistProfile implements MedicalProfile {

    @Override
    public String getProfileId() {
        return "CARDIOLOGO";
    }

    @Override
    public String getDisplayName() {
        return "Cardiologo";
    }

    @Override
    public String getTemplateName() {
        return "template_cardiologo.odt";
    }

    @Override
    public List<String> getBookmarkNames() {
        return Arrays.asList(
            "BM_ANAMNESI",
            "BM_DATI_ECG",
            "BM_ECOCARDIOGRAMMA",
            "BM_FREQUENZA_CARDIACA",
            "BM_FRAZIONE_EIEZIONE",
            "BM_VALVOLE",
            "BM_DIAGNOSI",
            "BM_TERAPIA"
        );
    }

    @Override
    public String getDescription() {
        return "Profilo per cardiologia con dati ECG, ecocardiogramma e parametri cardiaci.";
    }
}
