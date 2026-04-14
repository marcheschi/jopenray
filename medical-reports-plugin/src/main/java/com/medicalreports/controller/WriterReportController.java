package com.medicalreports.controller;

import com.medicalreports.database.DatabaseManager;
import com.medicalreports.model.Referto;
import com.medicalreports.profile.MedicalProfile;
import com.medicalreports.profile.TemplateManager;
import com.medicalreports.security.SecurityUtil;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.XBookmark;
import com.sun.star.text.XBookmarksSupplier;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Writer Report Editor.
 * Uses UNO API to load templates, inject data into bookmarks,
 * and extract content for saving to the database.
 * 
 * @author Medical Reports Team
 * @version 1.0.0
 */
public class WriterReportController {

    private static final Logger LOGGER = Logger.getLogger(WriterReportController.class.getName());

    private final XComponentContext context;
    private final DatabaseManager databaseManager;
    private final TemplateManager templateManager;
    private XTextDocument textDocument;
    private Referto currentReferto;

    /**
     * Creates a new WriterReportController.
     * 
     * @param context UNO component context
     * @param databaseManager Database manager instance
     */
    public WriterReportController(XComponentContext context, DatabaseManager databaseManager) {
        this.context = context;
        this.databaseManager = databaseManager;
        this.templateManager = TemplateManager.getInstance();
    }

    /**
     * Creates a new report document from a template based on specialization.
     * 
     * @param pazienteId Patient ID
     * @param specializzazione Specialization code
     * @return true if successful, false otherwise
     */
    public boolean createNewReport(UUID pazienteId, String specializzazione) {
        try {
            // Get the profile for this specialization
            MedicalProfile profile = templateManager.getProfile(specializzazione);
            if (profile == null) {
                LOGGER.severe("No profile found for specialization: " + specializzazione);
                return false;
            }

            // Create new Referto object
            currentReferto = new Referto(pazienteId, 
                Referto.Specializzazione.valueOf(specializzazione));

            // Load the template
            if (!loadTemplate(profile.getTemplateName())) {
                return false;
            }

            LOGGER.info("Created new report with template: " + profile.getTemplateName());
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating new report", e);
            return false;
        }
    }

    /**
     * Loads an existing report from the database into a Writer document.
     * 
     * @param refertoId Report ID
     * @return true if successful, false otherwise
     */
    public boolean loadExistingReport(UUID refertoId) {
        try {
            // Load report from database
            currentReferto = databaseManager.getRefertoById(refertoId);
            if (currentReferto == null) {
                LOGGER.severe("Report not found: " + refertoId);
                return false;
            }

            // Check if report is locked (signed)
            if (currentReferto.isFirmato()) {
                LOGGER.warning("Cannot modify signed report: " + refertoId);
                // Still load it in read-only mode
            }

            // Load the appropriate template
            MedicalProfile profile = templateManager.getProfile(
                currentReferto.getSpecializzazione().name());
            
            if (profile == null || !loadTemplate(profile.getTemplateName())) {
                return false;
            }

            // Inject data from database into bookmarks
            injectDataIntoBookmarks(currentReferto);

            LOGGER.info("Loaded existing report: " + refertoId);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading existing report", e);
            return false;
        }
    }

    /**
     * Loads a template document.
     * 
     * @param templateName Template filename
     * @return true if successful, false otherwise
     */
    private boolean loadTemplate(String templateName) {
        try {
            XMultiComponentFactory mcf = context.getServiceManager();
            Object desktopObject = mcf.createInstanceWithContext(
                "com.sun.star.frame.Desktop", context);
            XDesktop desktop = UnoRuntime.queryInterface(XDesktop.class, desktopObject);

            // In production, templates would be loaded from the extension package
            // For now, we'll create a new Writer document
            Object writerDoc = desktop.loadComponentFromURL(
                "private:factory/swriter", "_blank", 0, new PropertyValue[0]);

            textDocument = UnoRuntime.queryInterface(XTextDocument.class, writerDoc);

            if (textDocument == null) {
                LOGGER.severe("Failed to get text document");
                return false;
            }

            // Insert template header
            insertTemplateHeader(templateName);

            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading template: " + templateName, e);
            return false;
        }
    }

    /**
     * Inserts a header section into the document.
     * 
     * @param templateName Name of the template being used
     */
    private void insertTemplateHeader(String templateName) {
        try {
            XText text = textDocument.getText();
            XText cursor = text.createTextCursor();

            // Add header
            text.insertString(cursor, "REFERTO MEDICO\n", false);
            
            // Set header properties (bold, larger font)
            XPropertySet cursorProps = UnoRuntime.queryInterface(XPropertySet.class, cursor);
            if (cursorProps != null) {
                cursorProps.setPropertyValue("CharWeight", 150.0f); // Bold
                cursorProps.setPropertyValue("CharHeight", 14.0f);
            }

            text.insertString(cursor, "\n", false);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error inserting header", e);
        }
    }

    /**
     * Injects data from a Referto object into the document bookmarks.
     * 
     * @param referto The report data to inject
     */
    private void injectDataIntoBookmarks(Referto referto) {
        if (textDocument == null) {
            LOGGER.severe("Text document not initialized");
            return;
        }

        try {
            XBookmarksSupplier bookmarksSupplier = 
                UnoRuntime.queryInterface(XBookmarksSupplier.class, textDocument);
            
            if (bookmarksSupplier == null) {
                LOGGER.warning("Document does not support bookmarks");
                return;
            }

            // Get all bookmarks
            var bookmarks = bookmarksSupplier.getBookmarks();
            int bookmarkCount = bookmarks.getCount();

            LOGGER.info("Found " + bookmarkCount + " bookmarks in document");

            // Iterate through bookmarks and fill with data
            for (int i = 0; i < bookmarkCount; i++) {
                try {
                    Object bookmarkObj = bookmarks.getByIndex(i);
                    XBookmark bookmark = UnoRuntime.queryInterface(XBookmark.class, bookmarkObj);
                    
                    if (bookmark != null) {
                        String bookmarkName = bookmark.getName();
                        String content = getBookmarkContent(referto, bookmarkName);
                        
                        if (content != null) {
                            XTextContent anchor = bookmark.getAnchor();
                            XText text = UnoRuntime.queryInterface(XText.class, anchor);
                            
                            if (text != null) {
                                text.setString(content);
                                LOGGER.fine("Filled bookmark '" + bookmarkName + "' with content");
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.FINE, "Error processing bookmark", e);
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error injecting data into bookmarks", e);
        }
    }

    /**
     * Gets the content for a specific bookmark from the Referto.
     * 
     * @param referto The report data
     * @param bookmarkName The bookmark name
     * @return Content string or null if not applicable
     */
    private String getBookmarkContent(Referto referto, String bookmarkName) {
        // Map bookmark names to Referto fields
        switch (bookmarkName) {
            case "bm_anamnesi":
                return referto.getContenutoAnamnesi();
            case "bm_esame_obiettivo":
                return referto.getContenutoEsameObiettivo();
            case "bm_terapia":
                return referto.getContenutoTerapia();
            case "bm_diagnosi":
                return referto.getContenutoDiagnosi();
            case "bm_mappatura_lesioni":
            case "bm_dermoscopia":
            case "bm_dati_ecg":
            case "bm_ecocardiogramma":
            case "bm_fe":
            case "bm_valvole":
                return referto.getDatiSpecifici();
            default:
                return null;
        }
    }

    /**
     * Extracts content from document bookmarks and saves to database.
     * 
     * @return true if successful, false otherwise
     */
    public boolean saveReportToDatabase() {
        if (textDocument == null || currentReferto == null) {
            LOGGER.severe("Document or referto not initialized");
            return false;
        }

        // Check if report can be modified
        if (!currentReferto.isModificabile()) {
            LOGGER.warning("Cannot save - report is locked");
            return false;
        }

        try {
            // Extract content from bookmarks
            extractContentFromBookmarks();

            // Calculate SHA-256 hash for tamper evidence
            String contentForHash = SecurityUtil.calculateSHA256Concatenated(
                currentReferto.getContenutoAnamnesi(),
                currentReferto.getContenutoEsameObiettivo(),
                currentReferto.getContenutoTerapia(),
                currentReferto.getContenutoDiagnosi(),
                currentReferto.getDatiSpecifici()
            );
            currentReferto.setSha256Hash(contentForHash);

            // Save to database
            boolean saved = databaseManager.saveReferto(currentReferto);
            
            if (saved) {
                LOGGER.info("Report saved successfully: " + currentReferto.getId());
            } else {
                LOGGER.severe("Failed to save report to database");
            }

            return saved;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving report to database", e);
            return false;
        }
    }

    /**
     * Extracts content from document bookmarks into the Referto object.
     */
    private void extractContentFromBookmarks() {
        try {
            XBookmarksSupplier bookmarksSupplier = 
                UnoRuntime.queryInterface(XBookmarksSupplier.class, textDocument);
            
            if (bookmarksSupplier == null) {
                return;
            }

            var bookmarks = bookmarksSupplier.getBookmarks();
            int bookmarkCount = bookmarks.getCount();

            for (int i = 0; i < bookmarkCount; i++) {
                try {
                    Object bookmarkObj = bookmarks.getByIndex(i);
                    XBookmark bookmark = UnoRuntime.queryInterface(XBookmark.class, bookmarkObj);
                    
                    if (bookmark != null) {
                        String bookmarkName = bookmark.getName();
                        XTextContent anchor = bookmark.getAnchor();
                        XText text = UnoRuntime.queryInterface(XText.class, anchor);
                        
                        if (text != null) {
                            String content = text.getString();
                            setBookmarkContent(currentReferto, bookmarkName, content);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.FINE, "Error extracting bookmark content", e);
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error extracting content from bookmarks", e);
        }
    }

    /**
     * Sets content on a Referto based on bookmark name.
     * 
     * @param referto The report object
     * @param bookmarkName The bookmark name
     * @param content The content to set
     */
    private void setBookmarkContent(Referto referto, String bookmarkName, String content) {
        switch (bookmarkName) {
            case "bm_anamnesi":
                referto.setContenutoAnamnesi(content);
                break;
            case "bm_esame_obiettivo":
                referto.setContenutoEsameObiettivo(content);
                break;
            case "bm_terapia":
                referto.setContenutoTerapia(content);
                break;
            case "bm_diagnosi":
                referto.setContenutoDiagnosi(content);
                break;
            case "bm_mappatura_lesioni":
            case "bm_dermoscopia":
            case "bm_dati_ecg":
            case "bm_ecocardiogramma":
            case "bm_fe":
            case "bm_valvole":
                // Append to datiSpecifici or handle based on specialization
                String existing = referto.getDatiSpecifici();
                if (existing == null) {
                    referto.setDatiSpecifici(content);
                } else {
                    referto.setDatiSpecifici(existing + "\n" + content);
                }
                break;
        }
    }

    /**
     * Exports the current document to PDF using LibreOffice's native PDF exporter.
     * 
     * @param outputPath Full file path for the PDF output
     * @return true if successful, false otherwise
     */
    public boolean exportToPDF(String outputPath) {
        if (textDocument == null) {
            LOGGER.severe("Text document not initialized");
            return false;
        }

        try {
            XStorable xStorable = UnoRuntime.queryInterface(XStorable.class, textDocument);
            
            if (xStorable == null) {
                LOGGER.severe("Document does not support storage interface");
                return false;
            }

            // Setup PDF export filter
            PropertyValue[] filterData = new PropertyValue[0];
            PropertyValue[] props = new PropertyValue[2];
            
            props[0] = new PropertyValue();
            props[0].Name = "FilterName";
            props[0].Value = "writer_pdf_Export";
            
            props[1] = new PropertyValue();
            props[1].Name = "FilterData";
            props[1].Value = filterData;

            // Convert path to URL format
            String urlPath = "file:///" + outputPath.replace("\\", "/");

            // Export to PDF
            xStorable.storeToURL(urlPath, props);

            LOGGER.info("PDF exported successfully: " + outputPath);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting to PDF: " + outputPath, e);
            return false;
        }
    }

    /**
     * Updates the report status (BOZZA -> COMPLETATO -> FIRMATO -> ARCHIVIATO).
     * 
     * @param newStatus The new status
     * @return true if successful, false otherwise
     */
    public boolean updateStatus(Referto.Stato newStatus) {
        if (currentReferto == null) {
            LOGGER.severe("No current referto");
            return false;
        }

        // Check state transitions
        if (currentReferto.isFirmato() && newStatus != Referto.Stato.ARCHIVIATO) {
            LOGGER.warning("Cannot change status of signed report except to ARCHIVIATO");
            return false;
        }

        currentReferto.setStato(newStatus);
        
        // Save the status change
        return databaseManager.saveReferto(currentReferto);
    }

    /**
     * Gets the current Referto object.
     * 
     * @return Current Referto or null
     */
    public Referto getCurrentReferto() {
        return currentReferto;
    }

    /**
     * Gets the current text document.
     * 
     * @return XTextDocument or null
     */
    public XTextDocument getTextDocument() {
        return textDocument;
    }

    /**
     * Closes the document.
     */
    public void close() {
        if (textDocument != null) {
            try {
                com.sun.star.lang.XComponent comp = 
                    UnoRuntime.queryInterface(com.sun.star.lang.XComponent.class, textDocument);
                if (comp != null) {
                    comp.dispose();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing document", e);
            }
            textDocument = null;
        }
        currentReferto = null;
    }
}
