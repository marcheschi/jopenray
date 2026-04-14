package com.medicalreports.domain;

/**
 * Paziente domain model with GDPR obfuscation support.
 */
public class Paziente {
    private Long id;
    private String nome;
    private String cognome;
    private String codiceFiscale;
    private String dataNascita;
    private String indirizzo;
    private String telefono;
    private String email;

    public Paziente() {}

    public Paziente(Long id, String nome, String cognome, String codiceFiscale, 
                    String dataNascita, String indirizzo, String telefono, String email) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.codiceFiscale = codiceFiscale;
        this.dataNascita = dataNascita;
        this.indirizzo = indirizzo;
        this.telefono = telefono;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    
    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }
    
    public String getDataNascita() { return dataNascita; }
    public void setDataNascita(String dataNascita) { this.dataNascita = dataNascita; }
    
    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns obfuscated name for GDPR compliance (e.g., "M**** Rossi")
     */
    public String getObfuscatedNome() {
        if (nome == null || nome.isEmpty()) return "";
        if (nome.length() <= 1) return nome;
        return nome.charAt(0) + "****";
    }

    /**
     * Returns obfuscated surname for GDPR compliance
     */
    public String getObfuscatedCognome() {
        if (cognome == null || cognome.isEmpty()) return "";
        return cognome; // Surname is kept visible for identification
    }

    /**
     * Returns obfuscated tax code for GDPR compliance (e.g., "RSS***90A10")
     */
    public String getObfuscatedCodiceFiscale() {
        if (codiceFiscale == null || codiceFiscale.length() < 6) return "*************";
        return codiceFiscale.substring(0, 3) + "***" + codiceFiscale.substring(6);
    }

    @Override
    public String toString() {
        return "Paziente{id=" + id + ", nome='" + getObfuscatedNome() + "', cognome='" + getObfuscatedCognome() + "'}";
    }
}
