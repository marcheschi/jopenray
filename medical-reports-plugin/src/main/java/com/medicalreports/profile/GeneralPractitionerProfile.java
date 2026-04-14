package com.medicalreports.profile;

/**
 * Medical profile for General Practitioner (Medico di Medicina Generale).
 * Defines bookmarks for: Anamnesis, Objective Examination, Therapy.
 * 
 * @author Medical Reports Team
 * @version 1.0.0
 */
public class GeneralPractitionerProfile implements MedicalProfile {

    private static final String TEMPLATE_NAME = "template_gp.odt";
    private static final String DISPLAY_NAME = "Medico di Medicina Generale";

    // Bookmark names - must match exactly with bookmarks in the ODT template
    public static final String BOOKMARK_ANAMNESI = "bm_anamnesi";
    public static final String BOOKMARK_ESAME_OBIETTIVO = "bm_esame_obiettivo";
    public static final String BOOKMARK_TERAPIA = "bm_terapia";
    public static final String BOOKMARK_DIAGNOSI = "bm_diagnosi";

    private static final String[] BOOKMARKS = {
        BOOKMARK_ANAMNESI,
        BOOKMARK_ESAME_OBIETTIVO,
        BOOKMARK_TERAPIA,
        BOOKMARK_DIAGNOSI
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
            case BOOKMARK_ANAMNESI:
                return "Storia clinica del paziente, sintomi riferiti, patologie pregresse";
            case BOOKMARK_ESAME_OBIETTIVO:
                return "Risultati della visita medica, segni clinici osservati";
            case BOOKMARK_TERAPIA:
                return "Trattamento prescritto, farmaci, dosaggi e durata";
            case BOOKMARK_DIAGNOSI:
                return "Diagnosi formulata al termine della visita";
            default:
                return "Segnaposto non riconosciuto";
        }
    }

    @Override
    public String getSectionName(String bookmarkName) {
        switch (bookmarkName) {
            case BOOKMARK_ANAMNESI:
                return "Anamnesi";
            case BOOKMARK_ESAME_OBIETTIVO:
                return "Esame Obiettivo";
            case BOOKMARK_TERAPIA:
                return "Terapia";
            case BOOKMARK_DIAGNOSI:
                return "Diagnosi";
            default:
                return "Generale";
        }
    }
}
