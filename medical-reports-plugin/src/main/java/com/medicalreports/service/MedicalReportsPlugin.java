package com.medicalreports.service;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
import com.sun.star.uno.XComponentContext;

/**
 * Main entry point for the Medical Report Station LibreOffice Extension.
 * 
 * This class implements XJobExecutor to allow the extension to be triggered
 * from toolbar buttons, menu items, and other UNO dispatch commands.
 * 
 * @author Medical Reports Team
 * @version 1.0.0
 */
public class MedicalReportsPlugin implements XJobExecutor {

    private final XComponentContext context;
    private static final String ACTION_OPEN_DASHBOARD = "OpenDashboard";
    private static final String ACTION_NEW_REPORT = "NewReport";
    private static final String ACTION_SAVE_REPORT = "SaveReport";
    private static final String ACTION_EXPORT_PDF = "ExportPDF";
    private static final String ACTION_SEARCH_PATIENT = "SearchPatient";

    /**
     * Constructor called by LibreOffice when loading the extension.
     * 
     * @param context The UNO component context
     */
    public MedicalReportsPlugin(XComponentContext context) {
        this.context = context;
        System.out.println("[MedicalReportsPlugin] Initialized with context: " + context);
    }

    /**
     * Factory method for component registration.
     * 
     * @param implementationName The name of the implementation
     * @param serviceManager The service manager
     * @param registryKey The registry key
     * @return XSingleServiceFactory for the service
     */
    public static XSingleServiceFactory __getServiceFactory(
            String implementationName,
            XMultiComponentFactory serviceManager,
            XRegistryKey registryKey) {
        
        System.out.println("[MedicalReportsPlugin] getServiceFactory called for: " + implementationName);
        
        if (implementationName.equals(MedicalReportsPlugin.class.getName())) {
            return new com.sun.star.comp.helper.SingletonComponentFactory(
                MedicalReportsPlugin.class.getName(),
                MedicalReportsPlugin.class.getName()
            );
        }
        return null;
    }

    /**
     * Returns the implementation names for this service.
     * 
     * @return Array of implementation names
     */
    public static String[] __getImplementationNames() {
        return new String[] {
            "com.medicalreports.MedicalReportsPlugin",
            "com.sun.star.task.JobExecutor"
        };
    }

    /**
     * Returns the supported service names.
     * 
     * @return Array of service names
     */
    public static String[] __getServiceNames() {
        return new String[] {
            "com.sun.star.task.JobExecutor"
        };
    }

    /**
     * Executes a job based on the action parameter.
     * This method is called when a toolbar button or menu item triggers the extension.
     * 
     * @param action The action to execute (e.g., "OpenDashboard", "NewReport")
     */
    @Override
    public void trigger(String action) {
        System.out.println("[MedicalReportsPlugin] Trigger action: " + action);
        
        try {
            switch (action) {
                case ACTION_OPEN_DASHBOARD:
                    handleOpenDashboard();
                    break;
                case ACTION_NEW_REPORT:
                    handleNewReport();
                    break;
                case ACTION_SAVE_REPORT:
                    handleSaveReport();
                    break;
                case ACTION_EXPORT_PDF:
                    handleExportPDF();
                    break;
                case ACTION_SEARCH_PATIENT:
                    handleSearchPatient();
                    break;
                default:
                    System.err.println("[MedicalReportsPlugin] Unknown action: " + action);
            }
        } catch (Exception e) {
            System.err.println("[MedicalReportsPlugin] Error executing action " + action + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens the Calc dashboard for patient search and registry management.
     */
    private void handleOpenDashboard() {
        System.out.println("[MedicalReportsPlugin] Opening patient dashboard...");
        // Implementation will be added in Step 4 - Calc Integration
        // Will use CalcDashboardController to open/create .ods file
    }

    /**
     * Creates a new medical report using the appropriate template.
     */
    private void handleNewReport() {
        System.out.println("[MedicalReportsPlugin] Creating new report...");
        // Implementation will be added in Step 3 & 5
        // Will show profile selection dialog and load appropriate ODT template
    }

    /**
     * Saves the current Writer document to the H2 database.
     */
    private void handleSaveReport() {
        System.out.println("[MedicalReportsPlugin] Saving report to database...");
        // Implementation will be added in Step 5
        // Will extract bookmark data, encrypt, calculate SHA-256, save to H2
    }

    /**
     * Exports the current Writer document to PDF using UNO API.
     */
    private void handleExportPDF() {
        System.out.println("[MedicalReportsPlugin] Exporting to PDF...");
        // Implementation will be added in Step 5
        // Will use XStorable.storeToURL with writer_pdf_Export filter
    }

    /**
     * Handles patient search from the Calc dashboard.
     */
    private void handleSearchPatient() {
        System.out.println("[MedicalReportsPlugin] Searching patient...");
        // Implementation will be added in Step 4
        // Will filter database results and update Calc rows
    }

    /**
     * Gets the UNO component context.
     * 
     * @return The component context
     */
    public XComponentContext getContext() {
        return context;
    }
}
