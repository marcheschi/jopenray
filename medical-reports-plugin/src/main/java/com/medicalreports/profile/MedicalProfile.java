package com.medicalreports.profile;

/**
 * Interface for medical profile definitions.
 * Each specialization implements this interface to define
 * the specific bookmarks and fields required for that specialty.
 * 
 * @author Medical Reports Team
 * @version 1.0.0
 */
public interface MedicalProfile {

    /**
     * Gets the display name of the medical specialization.
     * 
     * @return Display name (e.g., "Medico di Medicina Generale")
     */
    String getDisplayName();

    /**
     * Gets the template filename for this profile.
     * 
     * @return Template filename (e.g., "template_gp.odt")
     */
    String getTemplateName();

    /**
     * Gets the list of bookmark names expected in the template.
     * These bookmarks will be populated with data from the database.
     * 
     * @return Array of bookmark names
     */
    String[] getBookmarkNames();

    /**
     * Gets the description of a specific bookmark.
     * Useful for UI tooltips and documentation.
     * 
     * @param bookmarkName The bookmark name
     * @return Description of the bookmark's purpose
     */
    String getBookmarkDescription(String bookmarkName);

    /**
     * Gets the section name for a specific bookmark.
     * Used for organizing the document structure.
     * 
     * @param bookmarkName The bookmark name
     * @return Section name (e.g., "Anamnesi", "Esame Obiettivo")
     */
    String getSectionName(String bookmarkName);
}
