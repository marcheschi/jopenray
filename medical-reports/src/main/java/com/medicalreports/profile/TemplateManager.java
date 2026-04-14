package com.medicalreports.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manager for medical profile templates.
 * Maps profiles to template files and defines expected bookmarks.
 */
public class TemplateManager {

    private static final Map<String, MedicalProfile> PROFILES = new HashMap<>();
    
    static {
        // Register all available profiles
        registerProfile(new GeneralPractitionerProfile());
        registerProfile(new DermatologistProfile());
        registerProfile(new CardiologistProfile());
    }

    /**
     * Registers a medical profile.
     */
    public static void registerProfile(MedicalProfile profile) {
        PROFILES.put(profile.getProfileId(), profile);
    }

    /**
     * Gets a profile by its ID.
     */
    public static Optional<MedicalProfile> getProfileById(String profileId) {
        return Optional.ofNullable(PROFILES.get(profileId));
    }

    /**
     * Gets all registered profiles.
     */
    public static List<MedicalProfile> getAllProfiles() {
        return PROFILES.values().stream().toList();
    }

    /**
     * Gets the template path for a given profile.
     * Templates are expected to be in the 'templates' folder of the extension.
     */
    public static String getTemplatePath(String profileId) {
        return getProfileById(profileId)
            .map(profile -> "vnd.sun.star.script://medical-reports/templates/" + profile.getTemplateName())
            .orElse(null);
    }

    /**
     * Gets the bookmark names for a given profile.
     */
    public static List<String> getBookmarkNames(String profileId) {
        return getProfileById(profileId)
            .map(MedicalProfile::getBookmarkNames)
            .orElse(List.of());
    }

    /**
     * Checks if a profile exists.
     */
    public static boolean hasProfile(String profileId) {
        return PROFILES.containsKey(profileId);
    }

    /**
     * Gets the display name for a profile.
     */
    public static String getDisplayName(String profileId) {
        return getProfileById(profileId)
            .map(MedicalProfile::getDisplayName)
            .orElse(profileId);
    }
}
