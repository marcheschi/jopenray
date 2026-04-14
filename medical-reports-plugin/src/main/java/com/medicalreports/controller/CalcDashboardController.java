package com.medicalreports.controller;

import com.medicalreports.database.DatabaseManager;
import com.medicalreports.model.Paziente;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.table.XCell;
import com.sun.star.table.XColumnRowRange;
import com.sun.star.table.XTableRows;
import com.sun.star.text.XText;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Calc Dashboard (Patient Registry).
 * Uses UNO API to create, populate, and manage the patient search spreadsheet.
 * 
 * @author Medical Reports Team
 * @version 1.0.0
 */
public class CalcDashboardController {

    private static final Logger LOGGER = Logger.getLogger(CalcDashboardController.class.getName());

    private final XComponentContext context;
    private final DatabaseManager databaseManager;
    private XSpreadsheetDocument spreadsheetDocument;
    private XSpreadsheet activeSheet;

    // Column indices for the dashboard
    private static final int COL_ID = 0;
    private static final int COL_COGNOME = 1;
    private static final int COL_NOME = 2;
    private static final int COL_CODICE_FISCALE = 3;
    private static final int COL_DATA_NASCITA = 4;
    private static final int COL_AZIONI = 5;

    // Header row index
    private static final int HEADER_ROW = 0;
    private static final int DATA_START_ROW = 1;

    /**
     * Creates a new CalcDashboardController.
     * 
     * @param context UNO component context
     * @param databaseManager Database manager instance
     */
    public CalcDashboardController(XComponentContext context, DatabaseManager databaseManager) {
        this.context = context;
        this.databaseManager = databaseManager;
    }

    /**
     * Opens or creates the patient dashboard Calc document.
     * Populates it with obfuscated patient data from the database.
     * 
     * @return true if successful, false otherwise
     */
    public boolean openDashboard() {
        try {
            // Get the desktop service
            XMultiComponentFactory mcf = context.getServiceManager();
            Object desktopObject = mcf.createInstanceWithContext(
                "com.sun.star.frame.Desktop", context);
            XDesktop desktop = UnoRuntime.queryInterface(XDesktop.class, desktopObject);

            // Create a new Calc document
            Object calcDoc = desktop.loadComponentFromURL(
                "private:factory/scalc", "_blank", 0, new PropertyValue[0]);

            spreadsheetDocument = UnoRuntime.queryInterface(XSpreadsheetDocument.class, calcDoc);
            
            if (spreadsheetDocument == null) {
                LOGGER.severe("Failed to get spreadsheet document");
                return false;
            }

            // Get the spreadsheets collection
            XSpreadsheets spreadsheets = spreadsheetDocument.getSheets();
            
            // Get the first sheet (index 0)
            Object sheetObj = spreadsheets.getByIndex(0);
            activeSheet = UnoRuntime.queryInterface(XSpreadsheet.class, sheetObj);

            if (activeSheet == null) {
                LOGGER.severe("Failed to get active spreadsheet");
                return false;
            }

            // Setup the dashboard
            setupDashboardHeaders();
            populateDashboardData();

            LOGGER.info("Dashboard opened successfully");
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening dashboard", e);
            return false;
        }
    }

    /**
     * Sets up the header row with column titles.
     */
    private void setupDashboardHeaders() {
        try {
            // Set header values
            setCellValue(HEADER_ROW, COL_ID, "ID");
            setCellValue(HEADER_ROW, COL_COGNOME, "Cognome");
            setCellValue(HEADER_ROW, COL_NOME, "Nome");
            setCellValue(HEADER_ROW, COL_CODICE_FISCALE, "Codice Fiscale");
            setCellValue(HEADER_ROW, COL_DATA_NASCITA, "Data Nascita");
            setCellValue(HEADER_ROW, COL_AZIONI, "Azioni");

            // Make headers bold (optional formatting)
            // This would require additional UNO calls to cell properties

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error setting up headers", e);
        }
    }

    /**
     * Populates the dashboard with patient data from the database.
     * Data is obfuscated for GDPR compliance.
     */
    private void populateDashboardData() {
        if (!databaseManager.isConnected()) {
            LOGGER.warning("Database not connected");
            return;
        }

        List<Paziente> pazienti = databaseManager.getAllPazienti();
        
        if (pazienti == null || pazienti.isEmpty()) {
            LOGGER.info("No patients found in database");
            return;
        }

        int rowIndex = DATA_START_ROW;
        for (Paziente paziente : pazienti) {
            try {
                // Clear existing row data first
                clearRow(rowIndex);

                // Set obfuscated data
                setCellValue(rowIndex, COL_ID, paziente.getId().toString());
                setCellValue(rowIndex, COL_COGNOME, paziente.getCognomeObfuscato(false)); // Keep surname visible for search
                setCellValue(rowIndex, COL_NOME, paziente.getNomeObfuscato());
                setCellValue(rowIndex, COL_CODICE_FISCALE, paziente.getCodiceFiscaleObfuscato());
                
                if (paziente.getDataNascita() != null) {
                    setCellValue(rowIndex, COL_DATA_NASCITA, 
                        paziente.getDataNascita().toLocalDate().toString());
                } else {
                    setCellValue(rowIndex, COL_DATA_NASCITA, "");
                }
                
                setCellValue(rowIndex, COL_AZIONI, "[Apri] [Modifica]");

                rowIndex++;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error writing patient row: " + paziente.getId(), e);
            }
        }

        LOGGER.info("Populated dashboard with " + pazienti.size() + " patients");
    }

    /**
     * Searches patients and updates the dashboard with filtered results.
     * 
     * @param searchQuery The search term
     * @return Number of results found
     */
    public int searchPatients(String searchQuery) {
        if (!databaseManager.isConnected()) {
            LOGGER.warning("Database not connected");
            return 0;
        }

        List<Paziente> risultati = databaseManager.searchPazienti(searchQuery);
        
        // Clear existing data rows
        clearDataRows();

        int rowIndex = DATA_START_ROW;
        for (Paziente paziente : risultati) {
            try {
                setCellValue(rowIndex, COL_ID, paziente.getId().toString());
                setCellValue(rowIndex, COL_COGNOME, paziente.getCognomeObfuscato(false));
                setCellValue(rowIndex, COL_NOME, paziente.getNomeObfuscato());
                setCellValue(rowIndex, COL_CODICE_FISCALE, paziente.getCodiceFiscaleObfuscato());
                
                if (paziente.getDataNascita() != null) {
                    setCellValue(rowIndex, COL_DATA_NASCITA, 
                        paziente.getDataNascita().toLocalDate().toString());
                }
                
                setCellValue(rowIndex, COL_AZIONI, "[Apri] [Modifica]");
                rowIndex++;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error writing search result row", e);
            }
        }

        LOGGER.info("Search returned " + risultati.size() + " results for: " + searchQuery);
        return risultati.size();
    }

    /**
     * Clears all data rows (excluding header).
     */
    private void clearDataRows() {
        try {
            XTableRows rows = UnoRuntime.queryInterface(XTableRows.class, 
                activeSheet.getRows());
            
            // Remove rows from index 1 onwards
            // Note: This is a simplified approach - in production, you'd want to
            // track the last used row and only clear those
            for (int i = rows.getCount() - 1; i >= DATA_START_ROW; i--) {
                rows.removeByIndex(i, 1);
            }
            
            // Re-insert enough rows for new data
            // This will be done automatically as we write data
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error clearing data rows", e);
        }
    }

    /**
     * Clears a specific row.
     * 
     * @param rowIndex Row index to clear
     */
    private void clearRow(int rowIndex) {
        try {
            for (int col = 0; col <= COL_AZIONI; col++) {
                XCell cell = activeSheet.getCellByPosition(col, rowIndex);
                XText text = UnoRuntime.queryInterface(XText.class, cell);
                if (text != null) {
                    text.setString("");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error clearing row " + rowIndex, e);
        }
    }

    /**
     * Sets a string value in a specific cell.
     * 
     * @param row Row index (0-based)
     * @param col Column index (0-based)
     * @param value Value to set
     */
    private void setCellValue(int row, int col, String value) {
        try {
            XCell cell = activeSheet.getCellByPosition(col, row);
            XText text = UnoRuntime.queryInterface(XText.class, cell);
            if (text != null) {
                text.setString(value);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error setting cell value at (" + row + "," + col + ")", e);
        }
    }

    /**
     * Refreshes the dashboard data from the database.
     */
    public void refresh() {
        clearDataRows();
        populateDashboardData();
    }

    /**
     * Gets the current spreadsheet document.
     * 
     * @return XSpreadsheetDocument or null
     */
    public XSpreadsheetDocument getSpreadsheetDocument() {
        return spreadsheetDocument;
    }

    /**
     * Closes the dashboard document.
     */
    public void close() {
        if (spreadsheetDocument != null) {
            try {
                com.sun.star.lang.XComponent comp = 
                    UnoRuntime.queryInterface(com.sun.star.lang.XComponent.class, spreadsheetDocument);
                if (comp != null) {
                    comp.dispose();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing spreadsheet", e);
            }
            spreadsheetDocument = null;
        }
    }
}
