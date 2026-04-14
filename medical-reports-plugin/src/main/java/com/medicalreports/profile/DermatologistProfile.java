package com.medicalreports.profile;

/**
 * Medical profile for Dermatologist (Dermatologo).
 * Defines bookmarks for: Lesion Mapping, Dermoscopy Findings.
 * 
 * @author Medical Reports Team
 * @version 1.0.0
 */
public class DermatologistProfile implements MedicalProfile {

    private static final String TEMPLATE_NAME = "template_dermatologo.odt";
    private static final String DISPLAY_NAME = "Dermatologo";

    // Bookmark names - must match exactly with bookmarks in the ODT template
    public static final String BOOKMARK_MAPPATURA_LESIONI = "bm_mappatura_lesioni";
    public static final String BOOKMARK_DERMOSCOPIA = "bm_dermoscopia";
    public static final String BOOKMARK_DIAGNOSI = "bm_diagnosi";
    public static final String BOOKMARK_TERAPIA = "bm_terapia";

    private static final String[] BOOKMARKS = {
        BOOKMARK_MAPPATURA_LESIONI,
        BOOKMARK_DERMOSCOPIA,
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
            case BOOKMARK_MAPPATURA_LESIONI:
                return "Descrizione e localizzazione delle lesioni cutanee";
            case BOOKMARK_DERMOSCOPIA:
                return "Reperti dermoscopici, strutture osservate, pattern";
            case BOOKMARK_DIAGNOSI:
                return "Diagnosi dermatologica formulata";
            case BOOKMARK_TERAPIA:
                return "Trattamento topico o sistemico prescritto";
            default:
                return "Segnaposto non riconosciuto";
        }
    }

    @Override
    public String getSectionName(String bookmarkName) {
        switch (bookmarkName) {
            case BOOKMARK_MAPPATURA_LESIONI:
                return "Mappatura Lesioni";
            case BOOKMARK_DERMOSCOPIA:
                return "Reperti Dermoscopici";
            case BOOKMARK_DIAGNOSI:
                return "Diagnosi";
            case BOOKMARK_TERAPIA:
                return "Terapia";
            default:
                return "Generale";
        }
    }
}
