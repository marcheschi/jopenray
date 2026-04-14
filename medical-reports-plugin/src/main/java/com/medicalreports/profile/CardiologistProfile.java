package com.medicalreports.profile;

/**
 * Medical profile for Cardiologist (Cardiologo).
 * Defines bookmarks for: ECG Data, Echocardiogram metrics.
 * 
 * @author Medical Reports Team
 * @version 1.0.0
 */
public class CardiologistProfile implements MedicalProfile {

    private static final String TEMPLATE_NAME = "template_cardiologo.odt";
    private static final String DISPLAY_NAME = "Cardiologo";

    // Bookmark names - must match exactly with bookmarks in the ODT template
    public static final String BOOKMARK_DATI_ECG = "bm_dati_ecg";
    public static final String BOOKMARK_ECOCARDIOGRAMMA = "bm_ecocardiogramma";
    public static final String BOOKMARK_FE = "bm_fe";           // Frazione di Eiezione
    public static final String BOOKMARK_VALVOLE = "bm_valvole"; // Stato valvole
    public static final String BOOKMARK_DIAGNOSI = "bm_diagnosi";
    public static final String BOOKMARK_TERAPIA = "bm_terapia";

    private static final String[] BOOKMARKS = {
        BOOKMARK_DATI_ECG,
        BOOKMARK_ECOCARDIOGRAMMA,
        BOOKMARK_FE,
        BOOKMARK_VALVOLE,
        BOOKMARK_DIAGNOSI,
        BOOKMARK_TERAPIA
    };

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getTemplateName() {
        return TEMPLATE_NAME;
    }

    @Override
    public String[] getBookmarkNames() {
        return BOOKMARKS.clone();
    }

    @Override
    public String getBookmarkDescription(String bookmarkName) {
        switch (bookmarkName) {
            case BOOKMARK_DATI_ECG:
                return "Ritmo cardiaco, frequenza, intervallo PR, QRS, QT";
            case BOOKMARK_ECOCARDIOGRAMMA:
                return "Descrizione dell'esame ecocardiografico";
            case BOOKMARK_FE:
                return "Frazione di Eiezione (EF%) del ventricolo sinistro";
            case BOOKMARK_VALVOLE:
                return "Stato e funzionalità delle valvole cardiache";
            case BOOKMARK_DIAGNOSI:
                return "Diagnosi cardiologica formulata";
            case BOOKMARK_TERAPIA:
                return "Trattamento cardiologico prescritto";
            default:
                return "Segnaposto non riconosciuto";
        }
    }

    @Override
    public String getSectionName(String bookmarkName) {
        switch (bookmarkName) {
            case BOOKMARK_DATI_ECG:
                return "Dati ECG";
            case BOOKMARK_ECOCARDIOGRAMMA:
                return "Ecocardiogramma";
            case BOOKMARK_FE:
                return "Frazione di Eiezione";
            case BOOKMARK_VALVOLE:
                return "Valvole";
            case BOOKMARK_DIAGNOSI:
                return "Diagnosi";
            case BOOKMARK_TERAPIA:
                return "Terapia";
            default:
                return "Generale";
        }
    }
}
