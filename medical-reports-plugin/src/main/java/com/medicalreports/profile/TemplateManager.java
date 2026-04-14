package com.medicalreports.profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager for medical profile templates.
 * Maps specializations to their profiles and template files.
 * 
 * @author Medical Reports Team
 * @version 1.0.0
 */
public class TemplateManager {

    private static final TemplateManager instance = new TemplateManager();
    
    private final Map<String, MedicalProfile> profilesBySpecialization;
    private final Map<String, String> templatePaths;
    
    // Base path for templates (can be configured)
    private String templateBasePath;

    private TemplateManager() {
        profilesBySpecialization = new HashMap<>();
        templatePaths = new HashMap<>();
        this.templateBasePath = "templates/";
        
        // Register default profiles
        registerProfile("MEDICO_GENERALE", new GeneralPractitionerProfile());
        registerProfile("DERMATOLOGO", new DermatologistProfile());
        registerProfile("CARDIOLOGO", new CardiologistProfile());
    }

    /**
     * Gets the singleton instance of TemplateManager.
     * 
     * @return TemplateManager instance
     */
    public static TemplateManager getInstance() {
        return instance;
    }

    /**
     * Registers a medical profile for a specialization.
     * 
     * @param specialization The specialization code
     * @param profile The medical profile implementation
     */
    public void registerProfile(String specialization, MedicalProfile profile) {
        profilesBySpecialization.put(specialization.toUpperCase(), profile);
        templatePaths.put(specialization.toUpperCase(), templateBasePath + profile.getTemplateName());
    }

    /**
     * Gets the medical profile for a specialization.
     * 
     * @param specialization The specialization code
     * @return MedicalProfile or null if not found
     */
    public MedicalProfile getProfile(String specialization) {
        return profilesBySpecialization.get(specialization.toUpperCase());
    }

    /**
     * Gets all registered specializations.
     * 
     * @return Array of specialization codes
     */
    public String[] getSpecializations() {
        return profilesBySpecialization.keySet().toArray(new String[0]);
    }

    /**
     * Gets the display name for a specialization.
     * 
     * @param specialization The specialization code
     * @return Display name or null if not found
     */
    public String getDisplayName(String specialization) {
        MedicalProfile profile = getProfile(specialization);
        return profile != null ? profile.getDisplayName() : null;
    }

    /**
     * Gets the full path to a template file.
     * 
     * @param specialization The specialization code
     * @return Full template path or null if not found
     */
    public String getTemplatePath(String specialization) {
        return templatePaths.get(specialization.toUpperCase());
    }

    /**
     * Gets the template filename for a specialization.
     * 
     * @param specialization The specialization code
     * @return Template filename or null if not found
     */
    public String getTemplateName(String specialization) {
        MedicalProfile profile = getProfile(specialization);
        return profile != null ? profile.getTemplateName() : null;
    }

    /**
     * Gets all bookmark names for a specialization.
     * 
     * @param specialization The specialization code
     * @return Array of bookmark names or null if not found
     */
    public String[] getBookmarkNames(String specialization) {
        MedicalProfile profile = getProfile(specialization);
        return profile != null ? profile.getBookmarkNames() : null;
    }

    /**
     * Sets the base path for templates.
     * 
     * @param path Base path (e.g., "templates/" or absolute path)
     */
    public void setTemplateBasePath(String path) {
        this.templateBasePath = path;
        
        // Update all template paths
        for (Map.Entry<String, MedicalProfile> entry : profilesBySpecialization.entrySet()) {
            templatePaths.put(entry.getKey(), path + entry.getValue().getTemplateName());
        }
    }

    /**
     * Gets the current template base path.
     * 
     * @return Base path string
     */
    public String getTemplateBasePath() {
        return templateBasePath;
    }

    /**
     * Validates that a bookmark exists for a given specialization.
     * 
     * @param specialization The specialization code
     * @param bookmarkName The bookmark name to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidBookmark(String specialization, String bookmarkName) {
        MedicalProfile profile = getProfile(specialization);
        if (profile == null) {
            return false;
        }
        
        for (String bm : profile.getBookmarkNames()) {
            if (bm.equals(bookmarkName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the section name for a bookmark in a specialization.
     * 
     * @param specialization The specialization code
     * @param bookmarkName The bookmark name
     * @return Section name or "Generale" if not found
     */
    public String getSectionName(String specialization, String bookmarkName) {
        MedicalProfile profile = getProfile(specialization);
        if (profile == null) {
            return "Generale";
        }
        return profile.getSectionName(bookmarkName);
    }

    /**
     * Gets the description for a bookmark in a specialization.
     * 
     * @param specialization The specialization code
     * @param bookmarkName The bookmark name
     * @return Description or empty string if not found
     */
    public String getBookmarkDescription(String specialization, String bookmarkName) {
        MedicalProfile profile = getProfile(specialization);
        if (profile == null) {
            return "";
        }
        return profile.getBookmarkDescription(bookmarkName);
    }
}
