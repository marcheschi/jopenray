package com.medicalreports.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a patient in the medical records system.
 * This class handles personal data that must be encrypted and GDPR-compliant.
 * 
 * @author Medical Reports Team
 * @version 1.0.0
 */
public class Paziente {

    private final UUID id;
    private String nome;
    private String cognome;
    private String codiceFiscale;
    private LocalDateTime dataNascita;
    private String indirizzo;
    private String telefono;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates a new patient with a random UUID.
     */
    public Paziente() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Creates a new patient with specified ID.
     * 
     * @param id The unique identifier for the patient
     */
    public Paziente(UUID id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(LocalDateTime dataNascita) {
        this.dataNascita = dataNascita;
        this.updatedAt = LocalDateTime.now();
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the full name of the patient.
     * 
     * @return Cognome e Nome
     */
    public String getNomeCompleto() {
        return cognome + " " + nome;
    }

    /**
     * Obfuscates the tax code for GDPR compliance.
     * Example: RSSMRA90A10H501X -> RSS***90A10
     * 
     * @return Obfuscated tax code
     */
    public String getCodiceFiscaleObfuscato() {
        if (codiceFiscale == null || codiceFiscale.length() < 11) {
            return "***";
        }
        // Keep first 3 chars, mask next 3, keep last 5
        return codiceFiscale.substring(0, 3) + "***" + codiceFiscale.substring(8, 11);
    }

    /**
     * Obfuscates the name for GDPR compliance.
     * Example: Mario Rossi -> M**** Rossi
     * 
     * @return Obfuscated name
     */
    public String getNomeObfuscato() {
        if (nome == null || nome.isEmpty()) {
            return "***";
        }
        return nome.charAt(0) + "****";
    }

    /**
     * Obfuscates the surname for GDPR compliance.
     * Example: Mario Rossi -> Rossi (surname is kept visible for search purposes)
     * In strict mode: R****
     * 
     * @param strictMode If true, obfuscate surname; if false, keep visible
     * @return Obfuscated or visible surname
     */
    public String getCognomeObfuscato(boolean strictMode) {
        if (cognome == null || cognome.isEmpty()) {
            return "***";
        }
        if (strictMode) {
            return cognome.charAt(0) + "****";
        }
        return cognome;
    }

    @Override
    public String toString() {
        return "Paziente{" +
                "id=" + id +
                ", nome='" + getNomeObfuscato() + '\'' +
                ", cognome='" + getCognomeObfuscato(false) + '\'' +
                ", codiceFiscale='" + getCodiceFiscaleObfuscato() + '\'' +
                '}';
    }
}
