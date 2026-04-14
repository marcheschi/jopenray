package com.medicalreports.controller;

import com.medicalreports.database.DatabaseManager;
import com.medicalreports.domain.Paziente;
import com.medicalreports.profile.MedicalProfile;
import com.medicalreports.profile.TemplateManager;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XComponent;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.table.XCell;
import com.sun.star.table.XColumnRowRange;
import com.sun.star.table.XTableColumns;
import com.sun.star.text.XText;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Calc dashboard operations.
 * Handles patient registry display with GDPR obfuscation.
 */
public class CalcDashboardController {

    private static final String BUNDLE_NAME = "messages_it";
    private final ResourceBundle messages;
    private final XComponentContext context;
    private final DatabaseManager dbManager;
    private XComponent calcDocument;

    public CalcDashboardController(XComponentContext context, DatabaseManager dbManager) {
        this.context = context;
        this.dbManager = dbManager;
        this.messages = ResourceBundle.getBundle(BUNDLE_NAME);
    }

    /**
     * Opens or creates the Calc dashboard document.
     */
    public void openDashboard() throws Exception {
        XDesktop desktop = getDesktop();
        
        // Try to find existing dashboard or create new one
        calcDocument = desktop.loadComponentFromURL(
            "private:factory/scalc", "_blank", 0, new com.sun.star.beans.PropertyValue[0]);
        
        if (calcDocument != null) {
            populateDashboard();
        }
    }

    /**
     * Populates the Calc sheet with obfuscated patient data.
     */
    public void populateDashboard() throws Exception {
        if (calcDocument == null) {
            return;
        }

        XSpreadsheets spreadsheets = UnoRuntime.queryInterface(
            XSpreadsheets.class, 
            calcDocument.getCurrentController().getFrames().getByName("Standard").getComponent());
        
        // Get the first sheet (or create "Ricerca Pazienti" sheet)
        XSpreadsheet sheet = spreadsheets.getByIndex(0);
        sheet.setName(messages.getString("dashboard.sheet.name"));

        // Clear existing data
        clearSheet(sheet);

        // Write headers
        writeHeaders(sheet);

        // Load patients and write obfuscated data
        List<Paziente> pazienti = dbManager.getAllPazienti();
        writePatientData(sheet, pazienti);

        // Format columns
        formatColumns(sheet);
    }

    /**
     * Searches patients and updates the dashboard.
     */
    public void searchAndPopulate(String query) throws Exception {
        if (calcDocument == null) {
            openDashboard();
        }

        List<Paziente> pazienti;
        if (query == null || query.trim().isEmpty()) {
            pazienti = dbManager.getAllPazienti();
        } else {
            pazienti = dbManager.searchPazienti(query);
        }

        XSpreadsheets spreadsheets = UnoRuntime.queryInterface(
            XSpreadsheets.class, 
            calcDocument.getCurrentController().getFrames().getByName("Standard").getComponent());
        XSpreadsheet sheet = spreadsheets.getByName(messages.getString("dashboard.sheet.name"));
        
        // Clear data rows (keep headers)
        clearDataRows(sheet);
        
        // Write filtered data
        writePatientData(sheet, pazienti);
    }

    /**
     * Writes header row to the sheet.
     */
    private void writeHeaders(XSpreadsheet sheet) throws Exception {
        String[] headers = {
            messages.getString("dashboard.header.id"),
            messages.getString("dashboard.header.nome"),
            messages.getString("dashboard.header.cognome"),
            messages.getString("dashboard.header.cf"),
            messages.getString("dashboard.header.data_nascita"),
            messages.getString("dashboard.header.telefono")
        };

        for (int i = 0; i < headers.length; i++) {
            XCell cell = sheet.getCellByPosition(i, 0);
            XText text = UnoRuntime.queryInterface(XText.class, cell);
            text.setString(headers[i]);
            
            // Bold header
            cell.setPropertyValue("CharWeight", 150.0);
            cell.setPropertyValue("CellBackColor", 0xD3D3D3); // Light gray
        }
    }

    /**
     * Writes patient data with GDPR obfuscation.
     */
    private void writePatientData(XSpreadsheet sheet, List<Paziente> pazienti) throws Exception {
        int row = 1; // Start after header
        
        for (Paziente paziente : pazienti) {
            // ID
            XCell cellId = sheet.getCellByPosition(0, row);
            cellId.setValue(paziente.getId());

            // Obfuscated Name (e.g., "M****")
            XCell cellNome = sheet.getCellByPosition(1, row);
            XText textNome = UnoRuntime.queryInterface(XText.class, cellNome);
            textNome.setString(paziente.getObfuscatedNome());

            // Surname (visible for identification)
            XCell cellCognome = sheet.getCellByPosition(2, row);
            XText textCognome = UnoRuntime.queryInterface(XText.class, cellCognome);
            textCognome.setString(paziente.getObfuscatedCognome());

            // Obfuscated Tax Code (e.g., "RSS***90A10")
            XCell cellCF = sheet.getCellByPosition(3, row);
            XText textCF = UnoRuntime.queryInterface(XText.class, cellCF);
            textCF.setString(paziente.getObfuscatedCodiceFiscale());

            // Birth Date
            XCell cellData = sheet.getCellByPosition(4, row);
            XText textData = UnoRuntime.queryInterface(XText.class, cellData);
            textData.setString(paziente.getDataNascita() != null ? paziente.getDataNascita() : "");

            // Obfuscated Phone
            XCell cellTel = sheet.getCellByPosition(5, row);
            XText textTel = UnoRuntime.queryInterface(XText.class, cellTel);
            String obfuscatedPhone = obfuscatePhone(paziente.getTelefono());
            textTel.setString(obfuscatedPhone);

            row++;
        }
    }

    /**
     * Obfuscates phone number for GDPR compliance.
     */
    private String obfuscatePhone(String phone) {
        if (phone == null || phone.length() <= 4) {
            return "****";
        }
        return phone.substring(0, 2) + "***" + phone.substring(phone.length() - 2);
    }

    /**
     * Clears all data from the sheet.
     */
    private void clearSheet(XSpreadsheet sheet) throws Exception {
        XColumnRowRange range = UnoRuntime.queryInterface(
            XColumnRowRange.class, sheet);
        XTableColumns columns = range.getColumns();
        
        // Clear all cells
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 10; j++) {
                XCell cell = sheet.getCellByPosition(j, i);
                XText text = UnoRuntime.queryInterface(XText.class, cell);
                text.setString("");
            }
        }
    }

    /**
     * Clears data rows only (keeps headers).
     */
    private void clearDataRows(XSpreadsheet sheet) throws Exception {
        for (int i = 1; i < 100; i++) {
            for (int j = 0; j < 10; j++) {
                XCell cell = sheet.getCellByPosition(j, i);
                XText text = UnoRuntime.queryInterface(XText.class, cell);
                text.setString("");
            }
        }
    }

    /**
     * Formats columns for better readability.
     */
    private void formatColumns(XSpreadsheet sheet) throws Exception {
        // Set column widths
        sheet.getColumns().getColumnByPosition(0).setPropertyPropertyValue("Width", 
            new com.sun.star.beans.PropertyValue());
        sheet.getColumns().getColumnByPosition(0).setSize((short) 500);
        sheet.getColumns().getColumnByPosition(1).setSize((short) 2000);
        sheet.getColumns().getColumnByPosition(2).setSize((short) 2500);
        sheet.getColumns().getColumnByPosition(3).setSize((short) 2000);
        sheet.getColumns().getColumnByPosition(4).setSize((short) 1800);
        sheet.getColumns().getColumnByPosition(5).setSize((short) 1800);
    }

    /**
     * Gets the LibreOffice Desktop service.
     */
    private XDesktop getDesktop() throws Exception {
        Object desktop = context.getServiceManager().createInstanceWithContext(
            "com.sun.star.frame.Desktop", context);
        return UnoRuntime.queryInterface(XDesktop.class, desktop);
    }

    /**
     * Closes the dashboard document.
     */
    public void closeDashboard() throws Exception {
        if (calcDocument != null) {
            calcDocument.dispose();
            calcDocument = null;
        }
    }

    /**
     * Gets the current Calc document.
     */
    public XComponent getCalcDocument() {
        return calcDocument;
    }
}
