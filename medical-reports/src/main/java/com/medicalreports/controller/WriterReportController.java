package com.medicalreports.controller;

import com.medicalreports.database.DatabaseManager;
import com.medicalreports.domain.Referto;
import com.medicalreports.profile.MedicalProfile;
import com.medicalreports.profile.TemplateManager;
import com.medicalreports.security.SecurityUtil;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyState;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XBookmark;
import com.sun.star.text.XBookmarksSupplier;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Controller for Writer report operations.
 * Handles template loading, bookmark injection, and PDF export.
 */
public class WriterReportController {

    private static final String BUNDLE_NAME = "messages_it";
    private final ResourceBundle messages;
    private final XComponentContext context;
    private final DatabaseManager dbManager;
    private XComponent writerDocument;
    private Referto currentReferto;
    private String currentProfileId;

    public WriterReportController(XComponentContext context, DatabaseManager dbManager) {
        this.context = context;
        this.dbManager = dbManager;
        this.messages = ResourceBundle.getBundle(BUNDLE_NAME);
    }

    /**
     * Creates a new report from a template based on the selected medical profile.
     */
    public void createNewReport(String profileId, Long pazienteId, String medicoResponsabile) throws Exception {
        MedicalProfile profile = TemplateManager.getProfileById(profileId)
            .orElseThrow(() -> new IllegalArgumentException("Profilo non trovato: " + profileId));

        currentProfileId = profileId;

        // Create new Referto
        currentReferto = new Referto(pazienteId, profileId, medicoResponsabile);

        // Load template
        loadTemplate(profile);

        if (writerDocument != null) {
            // Inject initial data into bookmarks
            injectInitialData();
        }
    }

    /**
     * Loads an existing report by ID.
     */
    public void loadReport(UUID refertoId) throws Exception {
        currentReferto = dbManager.getRefertoById(refertoId);
        
        if (currentReferto == null) {
            throw new IllegalArgumentException("Referto non trovato: " + refertoId);
        }

        currentProfileId = currentReferto.getSpecializzazione();
        MedicalProfile profile = TemplateManager.getProfileById(currentProfileId)
            .orElseThrow(() -> new IllegalArgumentException("Profilo non trovato: " + currentProfileId));

        // Load template
        loadTemplate(profile);

        if (writerDocument != null) {
            // Inject data from database
            injectDataFromReferto();
        }
    }

    /**
     * Loads a template for the given profile.
     */
    private void loadTemplate(MedicalProfile profile) throws Exception {
        XDesktop desktop = getDesktop();
        
        // For now, create a new document (templates would be packaged in the OXT)
        // In production, templates would be loaded from the extension package
        writerDocument = desktop.loadComponentFromURL(
            "private:factory/swriter", "_blank", 0, new PropertyValue[0]);

        if (writerDocument != null) {
            // Add title with profile name
            XTextDocument textDoc = UnoRuntime.queryInterface(XTextDocument.class, writerDocument);
            XText text = textDoc.getText();
            XText startText = text.getStartText();
            startText.setString(messages.getString("report.title") + " - " + profile.getDisplayName() + "\n\n");
        }
    }

    /**
     * Injects initial empty structure into bookmarks.
     */
    private void injectInitialData() throws Exception {
        if (writerDocument == null || currentProfileId == null) {
            return;
        }

        List<String> bookmarks = TemplateManager.getBookmarkNames(currentProfileId);
        XTextDocument textDoc = UnoRuntime.queryInterface(XTextDocument.class, writerDocument);

        for (String bookmarkName : bookmarks) {
            try {
                insertBookmark(textDoc, bookmarkName, "");
            } catch (Exception e) {
                // Bookmark might already exist or insertion failed
                System.err.println("Warning: Could not insert bookmark " + bookmarkName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Injects data from the loaded Referto into bookmarks.
     */
    private void injectDataFromReferto() throws Exception {
        if (writerDocument == null || currentReferto == null) {
            return;
        }

        XTextDocument textDoc = UnoRuntime.queryInterface(XTextDocument.class, writerDocument);

        // Inject standard fields
        insertOrUpdateBookmark(textDoc, "BM_ANAMNESI", currentReferto.getAnamnesi());
        insertOrUpdateBookmark(textDoc, "BM_ESAME_OBIETTIVO", currentReferto.getEsameObiettivo());
        insertOrUpdateBookmark(textDoc, "BM_TERAPIA", currentReferto.getTerapia());

        // Specialty-specific fields are handled by individual profiles
        // They would be injected similarly based on the profile type
    }

    /**
     * Inserts a bookmark with content into the document.
     */
    private void insertBookmark(XTextDocument textDoc, String name, String content) throws Exception {
        XBookmarksSupplier bookmarksSupplier = UnoRuntime.queryInterface(
            XBookmarksSupplier.class, textDoc);
        
        // Check if bookmark already exists
        if (bookmarksSupplier.getBookmarks().hasByName(name)) {
            // Update existing bookmark content
            updateBookmarkContent(textDoc, name, content);
            return;
        }

        // Create new bookmark at end of document
        XText text = textDoc.getText();
        XText endText = text.getEndText();
        
        // Insert content followed by bookmark
        endText.setString(content + "\n");
    }

    /**
     * Updates content of an existing bookmark.
     */
    private void updateBookmarkContent(XTextDocument textDoc, String bookmarkName, String content) throws Exception {
        XBookmarksSupplier bookmarksSupplier = UnoRuntime.queryInterface(
            XBookmarksSupplier.class, textDoc);
        
        Object bookmarkObj = bookmarksSupplier.getBookmarks().getByName(bookmarkName);
        XBookmark bookmark = UnoRuntime.queryInterface(XBookmark.class, bookmarkObj);
        
        // Get the text range of the bookmark and update it
        XTextContent anchor = bookmark.getAnchor();
        XTextRange range = UnoRuntime.queryInterface(XTextRange.class, anchor);
        
        if (range != null) {
            range.setString(content);
        }
    }

    /**
     * Inserts or updates a bookmark.
     */
    private void insertOrUpdateBookmark(XTextDocument textDoc, String name, String content) throws Exception {
        XBookmarksSupplier bookmarksSupplier = UnoRuntime.queryInterface(
            XBookmarksSupplier.class, textDoc);
        
        if (bookmarksSupplier.getBookmarks().hasByName(name)) {
            updateBookmarkContent(textDoc, name, content);
        } else {
            insertBookmark(textDoc, name, content);
        }
    }

    /**
     * Extracts content from bookmarks and saves to database.
     */
    public Referto saveToDatabase() throws Exception {
        if (writerDocument == null || currentReferto == null) {
            throw new IllegalStateException("Nessun documento o referto corrente");
        }

        if (!currentReferto.isModificabile()) {
            throw new IllegalStateException("Impossibile modificare un referto con stato: " + currentReferto.getStato());
        }

        XTextDocument textDoc = UnoRuntime.queryInterface(XTextDocument.class, writerDocument);

        // Extract content from bookmarks
        currentReferto.setAnamnesi(extractBookmarkContent(textDoc, "BM_ANAMNESI"));
        currentReferto.setEsameObiettivo(extractBookmarkContent(textDoc, "BM_ESAME_OBIETTIVO"));
        currentReferto.setTerapia(extractBookmarkContent(textDoc, "BM_TERAPIA"));

        // Calculate SHA-256 hash of raw content for tamper evidence
        String rawContent = currentReferto.getAnamnesi() + 
                           currentReferto.getEsameObiettivo() + 
                           currentReferto.getTerapia();
        currentReferto.setContenutoHash(SecurityUtil.calculateSHA256(rawContent));

        // Save or update in database
        if (dbManager.getRefertoById(currentReferto.getId()) == null) {
            dbManager.insertReferto(currentReferto);
        } else {
            dbManager.updateReferto(currentReferto);
        }

        return currentReferto;
    }

    /**
     * Extracts content from a specific bookmark.
     */
    private String extractBookmarkContent(XTextDocument textDoc, String bookmarkName) throws Exception {
        XBookmarksSupplier bookmarksSupplier = UnoRuntime.queryInterface(
            XBookmarksSupplier.class, textDoc);
        
        if (!bookmarksSupplier.getBookmarks().hasByName(bookmarkName)) {
            return "";
        }

        Object bookmarkObj = bookmarksSupplier.getBookmarks().getByName(bookmarkName);
        XBookmark bookmark = UnoRuntime.queryInterface(XBookmark.class, bookmarkObj);
        
        if (bookmark == null) {
            return "";
        }

        XTextContent anchor = bookmark.getAnchor();
        XTextRange range = UnoRuntime.queryInterface(XTextRange.class, anchor);
        
        if (range != null) {
            return range.getString();
        }

        return "";
    }

    /**
     * Exports the current Writer document to PDF using LibreOffice native exporter.
     */
    public void exportToPDF(String outputPath) throws Exception {
        if (writerDocument == null) {
            throw new IllegalStateException("Nessun documento aperto");
        }

        XStorable xStorable = UnoRuntime.queryInterface(XStorable.class, writerDocument);
        
        if (xStorable == null) {
            throw new IllegalStateException("Documento non esportabile");
        }

        // Prepare filter properties for PDF export
        PropertyValue[] filterData = new PropertyValue[0];
        PropertyValue[] props = new PropertyValue[2];
        
        props[0] = new PropertyValue();
        props[0].Name = "FilterName";
        props[0].Value = "writer_pdf_Export";
        props[0].State = PropertyState.DIRECT_VALUE;
        
        props[1] = new PropertyValue();
        props[1].Name = "FilterData";
        props[1].Value = filterData;
        props[1].State = PropertyState.DIRECT_VALUE;

        // Convert file path to URL format
        String urlPath = "file:///" + outputPath.replace("\\", "/");
        
        xStorable.storeToURL(urlPath, props);
        
        dbManager.logAudit(
            currentReferto != null ? currentReferto.getId().toString() : null,
            "EXPORT_PDF",
            "Esportato PDF: " + outputPath);
    }

    /**
     * Advances the workflow status of the current report.
     */
    public Referto advanceStatus() throws Exception {
        if (currentReferto == null) {
            throw new IllegalStateException("Nessun referto corrente");
        }

        if (currentReferto.advanceStatus()) {
            dbManager.updateReferto(currentReferto);
            
            dbManager.logAudit(
                currentReferto.getId().toString(),
                "STATUS_CHANGE",
                "Stato cambiato a: " + currentReferto.getStato());
        }

        return currentReferto;
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
     * Closes the Writer document.
     */
    public void closeDocument() throws Exception {
        if (writerDocument != null) {
            writerDocument.dispose();
            writerDocument = null;
        }
    }

    /**
     * Gets the current Writer document.
     */
    public XComponent getWriterDocument() {
        return writerDocument;
    }

    /**
     * Gets the current Referto.
     */
    public Referto getCurrentReferto() {
        return currentReferto;
    }

    /**
     * Interface for text range (needed for bookmark content extraction).
     */
    private interface XTextRange {
        String getString();
        void setString(String text);
    }
}
