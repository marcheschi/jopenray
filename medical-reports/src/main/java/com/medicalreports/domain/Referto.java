package com.medicalreports.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Referto domain model with workflow status management.
 */
public class Referto {
    
    public enum Stato {
        BOZZA,
        COMPLETATO,
        FIRMATO,
        ARCHIVIATO
    }

    private UUID id;
    private Long pazienteId;
    private String specializzazione;
    private String anamnesi;
    private String esameObiettivo;
    private String terapia;
    private String datiSpeciali; // JSON or XML for specialty-specific data
    private String contenutoHash; // SHA-256 hash for tamper evidence
    private Stato stato;
    private LocalDateTime dataCreazione;
    private LocalDateTime dataModifica;
    private LocalDateTime dataFirma;
    private String medicoResponsabile;

    public Referto() {
        this.id = UUID.randomUUID();
        this.stato = Stato.BOZZA;
        this.dataCreazione = LocalDateTime.now();
        this.dataModifica = LocalDateTime.now();
    }

    public Referto(Long pazienteId, String specializzazione, String medicoResponsabile) {
        this();
        this.pazienteId = pazienteId;
        this.specializzazione = specializzazione;
        this.medicoResponsabile = medicoResponsabile;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Long getPazienteId() { return pazienteId; }
    public void setPazienteId(Long pazienteId) { this.pazienteId = pazienteId; }
    
    public String getSpecializzazione() { return specializzazione; }
    public void setSpecializzazione(String specializzazione) { this.specializzazione = specializzazione; }
    
    public String getAnamnesi() { return anamnesi; }
    public void setAnamnesi(String anamnesi) { this.anamnesi = anamnesi; }
    
    public String getEsameObiettivo() { return esameObiettivo; }
    public void setEsameObiettivo(String esameObiettivo) { this.esameObiettivo = esameObiettivo; }
    
    public String getTerapia() { return terapia; }
    public void setTerapia(String terapia) { this.terapia = terapia; }
    
    public String getDatiSpeciali() { return datiSpeciali; }
    public void setDatiSpeciali(String datiSpeciali) { this.datiSpeciali = datiSpeciali; }
    
    public String getContenutoHash() { return contenutoHash; }
    public void setContenutoHash(String contenutoHash) { this.contenutoHash = contenutoHash; }
    
    public Stato getStato() { return stato; }
    public void setStato(Stato stato) { 
        this.stato = stato; 
        if (stato == Stato.FIRMATO && this.dataFirma == null) {
            this.dataFirma = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(LocalDateTime dataCreazione) { this.dataCreazione = dataCreazione; }
    
    public LocalDateTime getDataModifica() { return dataModifica; }
    public void setDataModifica(LocalDateTime dataModifica) { this.dataModifica = dataModifica; }
    
    public LocalDateTime getDataFirma() { return dataFirma; }
    public void setDataFirma(LocalDateTime dataFirma) { this.dataFirma = dataFirma; }
    
    public String getMedicoResponsabile() { return medicoResponsabile; }
    public void setMedicoResponsabile(String medicoResponsabile) { this.medicoResponsabile = medicoResponsabile; }

    /**
     * Checks if the report can be modified based on its status.
     */
    public boolean isModificabile() {
        return stato == Stato.BOZZA || stato == Stato.COMPLETATO;
    }

    /**
     * Advances the workflow status.
     */
    public boolean advanceStatus() {
        switch (this.stato) {
            case BOZZA:
                this.stato = Stato.COMPLETATO;
                this.dataModifica = LocalDateTime.now();
                return true;
            case COMPLETATO:
                this.stato = Stato.FIRMATO;
                this.dataFirma = LocalDateTime.now();
                return true;
            case FIRMATO:
                this.stato = Stato.ARCHIVIATO;
                return true;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return "Referto{id=" + id + ", pazienteId=" + pazienteId + 
               ", specializzazione='" + specializzazione + "', stato=" + stato + "}";
    }
}
