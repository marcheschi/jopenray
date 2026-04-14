package com.medicalreports.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a medical report (referto) in the system.
 * Contains encrypted sensitive data and audit information.
 * 
 * @author Medical Reports Team
 * @version 1.0.0
 */
public class Referto {

    public enum Stato {
        BOZZA,      // Draft - can be modified
        COMPLETATO, // Completed - ready for review
        FIRMATO,    // Signed - locked, cannot be modified
        ARCHIVIATO  // Archived - final state
    }

    public enum Specializzazione {
        MEDICO_GENERALE,      // General Practitioner
        DERMATOLOGO,          // Dermatologist
        CARDIOLOGO,           // Cardiologist
        PEDIATRA,             // Pediatrician
        ORTOPEDICO            // Orthopedist
    }

    private final UUID id;
    private final UUID pazienteId;
    private Specializzazione specializzazione;
    private String contenutoAnamnesi;      // Encrypted
    private String contenutoEsameObiettivo; // Encrypted
    private String contenutoTerapia;        // Encrypted
    private String contenutoDiagnosi;       // Encrypted
    private String datiSpecifici;           // Encrypted - specialty-specific data
    private String sha256Hash;              // Tamper-evidence hash
    private Stato stato;
    private LocalDateTime dataCreazione;
    private LocalDateTime dataModifica;
    private LocalDateTime dataFirma;
    private String medicoNome;
    private String medicoCognome;

    /**
     * Creates a new report in BOZZA state.
     * 
     * @param pazienteId The patient ID
     * @param specializzazione The medical specialization
     */
    public Referto(UUID pazienteId, Specializzazione specializzazione) {
        this.id = UUID.randomUUID();
        this.pazienteId = pazienteId;
        this.specializzazione = specializzazione;
        this.stato = Stato.BOZZA;
        this.dataCreazione = LocalDateTime.now();
        this.dataModifica = LocalDateTime.now();
    }

    /**
     * Creates a new report with specified ID.
     * 
     * @param id The unique identifier
     * @param pazienteId The patient ID
     * @param specializzazione The medical specialization
     */
    public Referto(UUID id, UUID pazienteId, Specializzazione specializzazione) {
        this.id = id;
        this.pazienteId = pazienteId;
        this.specializzazione = specializzazione;
        this.stato = Stato.BOZZA;
        this.dataCreazione = LocalDateTime.now();
        this.dataModifica = LocalDateTime.now();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public UUID getPazienteId() {
        return pazienteId;
    }

    public Specializzazione getSpecializzazione() {
        return specializzazione;
    }

    public void setSpecializzazione(Specializzazione specializzazione) {
        this.specializzazione = specializzazione;
        this.dataModifica = LocalDateTime.now();
    }

    public String getContenutoAnamnesi() {
        return contenutoAnamnesi;
    }

    public void setContenutoAnamnesi(String contenutoAnamnesi) {
        this.contenutoAnamnesi = contenutoAnamnesi;
        this.dataModifica = LocalDateTime.now();
    }

    public String getContenutoEsameObiettivo() {
        return contenutoEsameObiettivo;
    }

    public void setContenutoEsameObiettivo(String contenutoEsameObiettivo) {
        this.contenutoEsameObiettivo = contenutoEsameObiettivo;
        this.dataModifica = LocalDateTime.now();
    }

    public String getContenutoTerapia() {
        return contenutoTerapia;
    }

    public void setContenutoTerapia(String contenutoTerapia) {
        this.contenutoTerapia = contenutoTerapia;
        this.dataModifica = LocalDateTime.now();
    }

    public String getContenutoDiagnosi() {
        return contenutoDiagnosi;
    }

    public void setContenutoDiagnosi(String contenutoDiagnosi) {
        this.contenutoDiagnosi = contenutoDiagnosi;
        this.dataModifica = LocalDateTime.now();
    }

    public String getDatiSpecifici() {
        return datiSpecifici;
    }

    public void setDatiSpecifici(String datiSpecifici) {
        this.datiSpecifici = datiSpecifici;
        this.dataModifica = LocalDateTime.now();
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    public Stato getStato() {
        return stato;
    }

    public void setStato(Stato stato) {
        this.stato = stato;
        if (stato == Stato.FIRMATO) {
            this.dataFirma = LocalDateTime.now();
        }
        this.dataModifica = LocalDateTime.now();
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public LocalDateTime getDataModifica() {
        return dataModifica;
    }

    public LocalDateTime getDataFirma() {
        return dataFirma;
    }

    public String getMedicoNome() {
        return medicoNome;
    }

    public void setMedicoNome(String medicoNome) {
        this.medicoNome = medicoNome;
    }

    public String getMedicoCognome() {
        return medicoCognome;
    }

    public void setMedicoCognome(String medicoCognome) {
        this.medicoCognome = medicoCognome;
    }

    /**
     * Checks if the report can be modified.
     * Reports in FIRMATO or ARCHIVIATO state cannot be modified.
     * 
     * @return true if modifiable, false otherwise
     */
    public boolean isModificabile() {
        return stato == Stato.BOZZA || stato == Stato.COMPLETATO;
    }

    /**
     * Checks if the report is signed.
     * 
     * @return true if signed, false otherwise
     */
    public boolean isFirmato() {
        return stato == Stato.FIRMATO;
    }

    /**
     * Gets the template name based on specialization.
     * 
     * @return Template filename (e.g., "template_gp.odt")
     */
    public String getTemplateName() {
        switch (specializzazione) {
            case MEDICO_GENERALE:
                return "template_gp.odt";
            case DERMATOLOGO:
                return "template_dermatologo.odt";
            case CARDIOLOGO:
                return "template_cardiologo.odt";
            case PEDIATRA:
                return "template_pediatra.odt";
            case ORTOPEDICO:
                return "template_ortopedico.odt";
            default:
                return "template_generico.odt";
        }
    }

    @Override
    public String toString() {
        return "Referto{" +
                "id=" + id +
                ", pazienteId=" + pazienteId +
                ", specializzazione=" + specializzazione +
                ", stato=" + stato +
                ", dataCreazione=" + dataCreazione +
                '}';
    }
}
