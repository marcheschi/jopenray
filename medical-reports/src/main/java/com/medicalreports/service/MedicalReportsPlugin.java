package com.medicalreports.service;

import com.medicalreports.database.DatabaseManager;
import com.medicalreports.controller.CalcDashboardController;
import com.medicalreports.controller.WriterReportController;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.task.XJobExecutor;
import com.sun.star.uno.XComponentContext;

/**
 * Main service class for the Medical Reports Plugin.
 * Implements XJobExecutor to handle toolbar button clicks and menu actions.
 */
public class MedicalReportsPlugin implements XJobExecutor, XServiceInfo, XInitialization {

    private XComponentContext context;
    private DatabaseManager dbManager;
    private CalcDashboardController calcController;
    private WriterReportController writerController;

    @Override
    public void initialize(Object[] args) throws Exception {
        if (args.length > 0 && args[0] instanceof XComponentContext) {
            this.context = (XComponentContext) args[0];
            initializeServices();
        }
    }

    /**
     * Initializes backend services (database, controllers).
     */
    private void initializeServices() throws Exception {
        try {
            this.dbManager = new DatabaseManager();
            this.calcController = new CalcDashboardController(context, dbManager);
            this.writerController = new WriterReportController(context, dbManager);
        } catch (Exception e) {
            System.err.println("Error initializing Medical Reports Plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void trigger(String args) {
        try {
            switch (args) {
                case "open_dashboard":
                    openDashboard();
                    break;
                case "new_report":
                    newReport();
                    break;
                case "save_report":
                    saveReport();
                    break;
                case "export_pdf":
                    exportPDF();
                    break;
                case "advance_status":
                    advanceStatus();
                    break;
                default:
                    System.out.println("Unknown command: " + args);
            }
        } catch (Exception e) {
            System.err.println("Error executing command " + args + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens the Calc dashboard for patient search.
     */
    private void openDashboard() throws Exception {
        if (calcController != null) {
            calcController.openDashboard();
            dbManager.logAudit(null, "OPEN_DASHBOARD", "Aperta dashboard pazienti");
        }
    }

    /**
     * Creates a new report (shows profile selection dialog in full implementation).
     */
    private void newReport() throws Exception {
        // In full implementation, this would show a UNO dialog to select:
        // - Patient (from search)
        // - Medical Profile (GP, Dermatologist, Cardiologist)
        
        // For now, create a sample GP report
        if (writerController != null) {
            writerController.createNewReport("GP", 1L, "Dr. Mario Rossi");
            dbManager.logAudit(null, "NEW_REPORT", "Creato nuovo referto GP");
        }
    }

    /**
     * Saves the current report to the database.
     */
    private void saveReport() throws Exception {
        if (writerController != null) {
            writerController.saveToDatabase();
            dbManager.logAudit(
                writerController.getCurrentReferto() != null ? 
                    writerController.getCurrentReferto().getId().toString() : null,
                "SAVE_REPORT",
                "Salvato referto nel database");
        }
    }

    /**
     * Exports the current report to PDF.
     */
    private void exportPDF() throws Exception {
        if (writerController != null) {
            String outputPath = System.getProperty("user.home") + "/report_" + 
                System.currentTimeMillis() + ".pdf";
            writerController.exportToPDF(outputPath);
        }
    }

    /**
     * Advances the workflow status of the current report.
     */
    private void advanceStatus() throws Exception {
        if (writerController != null) {
            writerController.advanceStatus();
        }
    }

    @Override
    public boolean supportsService(String serviceName) {
        return "com.medicalreports.MedicalReportsPlugin".equals(serviceName);
    }

    @Override
    public String[] getSupportedServiceNames() {
        return new String[] {
            "com.medicalreports.MedicalReportsPlugin",
            "com.sun.star.task.Job"
        };
    }

    /**
     * Gets the implementation name.
     */
    public String getImplementationName() {
        return "com.medicalreports.MedicalReportsPlugin";
    }

    /**
     * Cleanup resources on shutdown.
     */
    public void dispose() {
        try {
            if (calcController != null) {
                calcController.closeDashboard();
            }
            if (writerController != null) {
                writerController.closeDocument();
            }
            if (dbManager != null) {
                dbManager.close();
            }
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}
