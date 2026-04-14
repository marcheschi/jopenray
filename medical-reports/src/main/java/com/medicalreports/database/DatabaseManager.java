package com.medicalreports.database;

import com.medicalreports.domain.Paziente;
import com.medicalreports.domain.Referto;
import com.medicalreports.security.SecurityUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Database manager for H2 embedded database with encryption support.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:~/medical-reports-db;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private Connection connection;
    private SecurityUtil securityUtil;

    public DatabaseManager() throws Exception {
        this.securityUtil = new SecurityUtil();
        initializeDatabase();
    }

    /**
     * Initializes the database connection and creates tables if they don't exist.
     */
    private void initializeDatabase() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        
        // Create Pazienti table
        String createPazientiTable = """
            CREATE TABLE IF NOT EXISTS PAZIENTI (
                ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                NOME VARCHAR(100),
                COGNOME VARCHAR(100) NOT NULL,
                CODICE_FISCALE_ENCRYPTED VARCHAR(512),
                DATA_NASCITA DATE,
                INDIRIZZO_ENCRYPTED VARCHAR(1024),
                TELEFONO_ENCRYPTED VARCHAR(256),
                EMAIL_ENCRYPTED VARCHAR(256),
                DATA_CREAZIONE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                DATA_MODIFICA TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        // Create Referti table
        String createRefertiTable = """
            CREATE TABLE IF NOT EXISTS REFERTI (
                ID VARCHAR(36) PRIMARY KEY,
                PAZIENTE_ID BIGINT NOT NULL,
                SPECIALIZZAZIONE VARCHAR(50) NOT NULL,
                ANAMNESI_ENCRYPTED TEXT,
                ESAME_OBIETTIVO_ENCRYPTED TEXT,
                TERAPIA_ENCRYPTED TEXT,
                DATI_SPECIALI_ENCRYPTED TEXT,
                CONTENUTO_HASH VARCHAR(64),
                STATO VARCHAR(20) NOT NULL,
                DATA_CREAZIONE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                DATA_MODIFICA TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                DATA_FIRMA TIMESTAMP,
                MEDICO_RESPONSABILE VARCHAR(200),
                FOREIGN KEY (PAZIENTE_ID) REFERENCES PAZIENTI(ID)
            )
            """;
        
        // Create Audit Log table
        String createAuditLogTable = """
            CREATE TABLE IF NOT EXISTS AUDIT_LOG (
                ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                UUID_RIFERIMENTO VARCHAR(36),
                AZIONE VARCHAR(100) NOT NULL,
                DESCRIZIONE TEXT,
                DATA_ORA TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UTENTE VARCHAR(100)
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPazientiTable);
            stmt.execute(createRefertiTable);
            stmt.execute(createAuditLogTable);
        }
    }

    /**
     * Inserts a new patient into the database.
     */
    public Paziente insertPaziente(Paziente paziente) throws Exception {
        String sql = """
            INSERT INTO PAZIENTI (NOME, COGNOME, CODICE_FISCALE_ENCRYPTED, DATA_NASCITA, 
                                  INDIRIZZO_ENCRYPTED, TELEFONO_ENCRYPTED, EMAIL_ENCRYPTED)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, paziente.getNome());
            pstmt.setString(2, paziente.getCognome());
            pstmt.setString(3, securityUtil.encrypt(paziente.getCodiceFiscale()));
            
            if (paziente.getDataNascita() != null) {
                pstmt.setDate(4, Date.valueOf(paziente.getDataNascita()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            
            pstmt.setString(5, securityUtil.encrypt(paziente.getIndirizzo()));
            pstmt.setString(6, securityUtil.encrypt(paziente.getTelefono()));
            pstmt.setString(7, securityUtil.encrypt(paziente.getEmail()));
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    paziente.setId(rs.getLong(1));
                    logAudit(paziente.getId().toString(), "INSERT_PAZIENTE", 
                             "Inserito nuovo paziente: " + paziente.getCognome());
                }
            }
        }
        
        return paziente;
    }

    /**
     * Retrieves all patients from the database.
     */
    public List<Paziente> getAllPazienti() throws Exception {
        List<Paziente> pazienti = new ArrayList<>();
        String sql = "SELECT * FROM PAZIENTI ORDER BY COGNOME, NOME";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Paziente paziente = new Paziente();
                paziente.setId(rs.getLong("ID"));
                paziente.setNome(rs.getString("NOME"));
                paziente.setCognome(rs.getString("COGNOME"));
                paziente.setCodiceFiscale(securityUtil.decrypt(rs.getString("CODICE_FISCALE_ENCRYPTED")));
                
                Date dataNascita = rs.getDate("DATA_NASCITA");
                if (dataNascita != null) {
                    paziente.setDataNascita(dataNascita.toString());
                }
                
                paziente.setIndirizzo(securityUtil.decrypt(rs.getString("INDIRIZZO_ENCRYPTED")));
                paziente.setTelefono(securityUtil.decrypt(rs.getString("TELEFONO_ENCRYPTED")));
                paziente.setEmail(securityUtil.decrypt(rs.getString("EMAIL_ENCRYPTED")));
                
                pazienti.add(paziente);
            }
        }
        
        return pazienti;
    }

    /**
     * Searches for patients by name or surname.
     */
    public List<Paziente> searchPazienti(String query) throws Exception {
        List<Paziente> pazienti = new ArrayList<>();
        String sql = """
            SELECT * FROM PAZIENTI 
            WHERE NOME LIKE ? OR COGNOME LIKE ? OR CODICE_FISCALE_ENCRYPTED LIKE ?
            ORDER BY COGNOME, NOME
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + query.toUpperCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Paziente paziente = new Paziente();
                    paziente.setId(rs.getLong("ID"));
                    paziente.setNome(rs.getString("NOME"));
                    paziente.setCognome(rs.getString("COGNOME"));
                    paziente.setCodiceFiscale(securityUtil.decrypt(rs.getString("CODICE_FISCALE_ENCRYPTED")));
                    
                    Date dataNascita = rs.getDate("DATA_NASCITA");
                    if (dataNascita != null) {
                        paziente.setDataNascita(dataNascita.toString());
                    }
                    
                    paziente.setIndirizzo(securityUtil.decrypt(rs.getString("INDIRIZZO_ENCRYPTED")));
                    paziente.setTelefono(securityUtil.decrypt(rs.getString("TELEFONO_ENCRYPTED")));
                    paziente.setEmail(securityUtil.decrypt(rs.getString("EMAIL_ENCRYPTED")));
                    
                    pazienti.add(paziente);
                }
            }
        }
        
        logAudit(null, "SEARCH_PAZIENTI", "Ricerca pazienti con query: " + query);
        return pazienti;
    }

    /**
     * Inserts a new report into the database.
     */
    public Referto insertReferto(Referto referto) throws Exception {
        String sql = """
            INSERT INTO REFERTI (ID, PAZIENTE_ID, SPECIALIZZAZIONE, ANAMNESI_ENCRYPTED,
                                 ESAME_OBIETTIVO_ENCRYPTED, TERAPIA_ENCRYPTED, DATI_SPECIALI_ENCRYPTED,
                                 CONTENUTO_HASH, STATO, MEDICO_RESPONSABILE)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, referto.getId().toString());
            pstmt.setLong(2, referto.getPazienteId());
            pstmt.setString(3, referto.getSpecializzazione());
            pstmt.setString(4, securityUtil.encrypt(referto.getAnamnesi()));
            pstmt.setString(5, securityUtil.encrypt(referto.getEsameObiettivo()));
            pstmt.setString(6, securityUtil.encrypt(referto.getTerapia()));
            pstmt.setString(7, securityUtil.encrypt(referto.getDatiSpeciali()));
            pstmt.setString(8, referto.getContenutoHash());
            pstmt.setString(9, referto.getStato().name());
            pstmt.setString(10, referto.getMedicoResponsabile());
            
            pstmt.executeUpdate();
            
            logAudit(referto.getId().toString(), "INSERT_REFERTO", 
                     "Inserito nuovo referto per paziente ID: " + referto.getPazienteId());
        }
        
        return referto;
    }

    /**
     * Updates an existing report in the database.
     */
    public Referto updateReferto(Referto referto) throws Exception {
        String sql = """
            UPDATE REFERTI SET 
                ANAMNESI_ENCRYPTED = ?,
                ESAME_OBIETTIVO_ENCRYPTED = ?,
                TERAPIA_ENCRYPTED = ?,
                DATI_SPECIALI_ENCRYPTED = ?,
                CONTENUTO_HASH = ?,
                STATO = ?,
                DATA_MODIFICA = CURRENT_TIMESTAMP
            WHERE ID = ?
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, securityUtil.encrypt(referto.getAnamnesi()));
            pstmt.setString(2, securityUtil.encrypt(referto.getEsameObiettivo()));
            pstmt.setString(3, securityUtil.encrypt(referto.getTerapia()));
            pstmt.setString(4, securityUtil.encrypt(referto.getDatiSpeciali()));
            pstmt.setString(5, referto.getContenutoHash());
            pstmt.setString(6, referto.getStato().name());
            pstmt.setString(7, referto.getId().toString());
            
            pstmt.executeUpdate();
            
            logAudit(referto.getId().toString(), "UPDATE_REFERTO", 
                     "Aggiornato referto stato: " + referto.getStato());
        }
        
        return referto;
    }

    /**
     * Retrieves a report by its ID.
     */
    public Referto getRefertoById(UUID id) throws Exception {
        String sql = "SELECT * FROM REFERTI WHERE ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Referto referto = new Referto();
                    referto.setId(UUID.fromString(rs.getString("ID")));
                    referto.setPazienteId(rs.getLong("PAZIENTE_ID"));
                    referto.setSpecializzazione(rs.getString("SPECIALIZZAZIONE"));
                    referto.setAnamnesi(securityUtil.decrypt(rs.getString("ANAMNESI_ENCRYPTED")));
                    referto.setEsameObiettivo(securityUtil.decrypt(rs.getString("ESAME_OBIETTIVO_ENCRYPTED")));
                    referto.setTerapia(securityUtil.decrypt(rs.getString("TERAPIA_ENCRYPTED")));
                    referto.setDatiSpeciali(securityUtil.decrypt(rs.getString("DATI_SPECIALI_ENCRYPTED")));
                    referto.setContenutoHash(rs.getString("CONTENUTO_HASH"));
                    referto.setStato(Referto.Stato.valueOf(rs.getString("STATO")));
                    referto.setMedicoResponsabile(rs.getString("MEDICO_RESPONSABILE"));
                    referto.setDataCreazione(rs.getTimestamp("DATA_CREAZIONE").toLocalDateTime());
                    referto.setDataModifica(rs.getTimestamp("DATA_MODIFICA").toLocalDateTime());
                    
                    Timestamp dataFirma = rs.getTimestamp("DATA_FIRMA");
                    if (dataFirma != null) {
                        referto.setDataFirma(dataFirma.toLocalDateTime());
                    }
                    
                    logAudit(id.toString(), "READ_REFERTO", "Lettura referto");
                    return referto;
                }
            }
        }
        
        return null;
    }

    /**
     * Retrieves all reports for a specific patient.
     */
    public List<Referto> getRefertiByPazienteId(Long pazienteId) throws Exception {
        List<Referto> referti = new ArrayList<>();
        String sql = "SELECT * FROM REFERTI WHERE PAZIENTE_ID = ? ORDER BY DATA_CREAZIONE DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, pazienteId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Referto referto = new Referto();
                    referto.setId(UUID.fromString(rs.getString("ID")));
                    referto.setPazienteId(rs.getLong("PAZIENTE_ID"));
                    referto.setSpecializzazione(rs.getString("SPECIALIZZAZIONE"));
                    referto.setAnamnesi(securityUtil.decrypt(rs.getString("ANAMNESI_ENCRYPTED")));
                    referto.setEsameObiettivo(securityUtil.decrypt(rs.getString("ESAME_OBIETTIVO_ENCRYPTED")));
                    referto.setTerapia(securityUtil.decrypt(rs.getString("TERAPIA_ENCRYPTED")));
                    referto.setDatiSpeciali(securityUtil.decrypt(rs.getString("DATI_SPECIALI_ENCRYPTED")));
                    referto.setContenutoHash(rs.getString("CONTENUTO_HASH"));
                    referto.setStato(Referto.Stato.valueOf(rs.getString("STATO")));
                    referto.setMedicoResponsabile(rs.getString("MEDICO_RESPONSABILE"));
                    referto.setDataCreazione(rs.getTimestamp("DATA_CREAZIONE").toLocalDateTime());
                    referto.setDataModifica(rs.getTimestamp("DATA_MODIFICA").toLocalDateTime());
                    
                    Timestamp dataFirma = rs.getTimestamp("DATA_FIRMA");
                    if (dataFirma != null) {
                        referto.setDataFirma(dataFirma.toLocalDateTime());
                    }
                    
                    referti.add(referto);
                }
            }
        }
        
        return referti;
    }

    /**
     * Logs an audit entry.
     */
    public void logAudit(String uuidRiferimento, String azione, String descrizione) throws SQLException {
        String sql = "INSERT INTO AUDIT_LOG (UUID_RIFERIMENTO, AZIONE, DESCRIZIONE, UTENTE) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuidRiferimento);
            pstmt.setString(2, azione);
            pstmt.setString(3, descrizione);
            pstmt.setString(4, System.getProperty("user.name", "unknown"));
            
            pstmt.executeUpdate();
        }
    }

    /**
     * Closes the database connection.
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Gets the security utility for external encryption/decryption needs.
     */
    public SecurityUtil getSecurityUtil() {
        return securityUtil;
    }
}
