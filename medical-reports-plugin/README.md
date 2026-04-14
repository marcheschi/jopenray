# Medical Report Station - LibreOffice Extension

A complete LibreOffice Extension (Java-based) for small doctor offices that leverages LibreOffice Calc as a Search Dashboard and LibreOffice Writer as a dynamic report editor, with H2 database backend for secure, GDPR-compliant medical record management.

## Features

### Core Functionality
- **LibreOffice Calc as Dashboard**: Patient registry with GDPR-obfuscated data display
- **LibreOffice Writer as Editor**: Native document editing with bookmark-based data injection
- **Dynamic Medical Profiles**: Specialization-specific templates (GP, Dermatologist, Cardiologist)
- **H2 Database Backend**: Encrypted storage with AES-GCM (256-bit)
- **Tamper Evidence**: SHA-256 hashing of document content
- **Status Workflow**: BOZZA → COMPLETATO → FIRMATO → ARCHIVIATO
- **Audit Logging**: Complete tracking of all operations
- **Native PDF Export**: Using LibreOffice's built-in PDF exporter

### Security & Compliance
- AES-GCM encryption for all sensitive fields (Tax Code, Diagnosis, etc.)
- SHA-256 hash verification for tamper detection
- GDPR-compliant data obfuscation in UI
- Audit trail for all database operations

## Project Structure

```
medical-reports-plugin/
├── pom.xml                          # Maven build configuration
├── src/
│   ├── assembly/
│   │   └── oxt.xml                  # OXT packaging descriptor
│   ├── main/
│   │   ├── java/com/medicalreports/
│   │   │   ├── service/
│   │   │   │   └── MedicalReportsPlugin.java    # Main UNO service entry point
│   │   │   ├── model/
│   │   │   │   ├── Paziente.java                # Patient domain model
│   │   │   │   └── Referto.java                 # Medical report domain model
│   │   │   ├── security/
│   │   │   │   └── SecurityUtil.java            # AES-GCM & SHA-256 utilities
│   │   │   ├── database/
│   │   │   │   └── DatabaseManager.java         # H2 CRUD operations
│   │   │   ├── profile/
│   │   │   │   ├── MedicalProfile.java          # Profile interface
│   │   │   │   ├── GeneralPractitionerProfile.java
│   │   │   │   ├── DermatologistProfile.java
│   │   │   │   ├── CardiologistProfile.java
│   │   │   │   └── TemplateManager.java         # Template registry
│   │   │   ├── controller/
│   │   │   │   ├── CalcDashboardController.java # Calc UNO integration
│   │   │   │   └── WriterReportController.java  # Writer UNO integration
│   │   │   ├── dialog/                        # (To be implemented)
│   │   │   └── util/                          # (To be implemented)
│   │   └── resources/com/medicalreports/
│   │       └── messages_it.properties         # Italian localization
└── description/
    ├── description.xml              # Extension metadata
    ├── Addons.xcu                   # Toolbar/menu configuration
    └── protocolhandler.xcu          # Protocol handler registration
```

## Technical Stack

- **Language**: Java 11+
- **Build Tool**: Maven
- **Integration**: LibreOffice UNO API
- **Database**: H2 Embedded (AES-256 encrypted)
- **Encryption**: AES-GCM (authenticated encryption)
- **Hashing**: SHA-256 (tamper evidence)

## Prerequisites

1. **Java Development Kit (JDK) 11 or higher**
   ```bash
   java -version  # Should show 11+
   ```

2. **Apache Maven**
   ```bash
   mvn -version
   ```

3. **LibreOffice SDK** (optional, for local UNO JARs)
   - Download from: https://www.libreoffice.org/download/sdk/
   - Or use Maven Central dependencies (configured in pom.xml)

## Building the Extension

### Step 1: Clone and Navigate
```bash
cd medical-reports-plugin
```

### Step 2: Compile
```bash
mvn clean compile
```

### Step 3: Package as OXT
```bash
mvn package
```

This will create:
- `target/medical-reports-station-1.0.0.jar` (the OXT file)
- `target/lib/` (runtime dependencies)

### Step 4: Install in LibreOffice

1. Open LibreOffice
2. Go to **Tools → Extension Manager**
3. Click **Add** and select `target/medical-reports-station-1.0.0.jar`
4. Accept the license agreement
5. Restart LibreOffice

## Usage

### Opening the Dashboard
1. Click the **Dashboard** button in the Medical Reports toolbar
2. A Calc spreadsheet opens with patient list (obfuscated data)
3. Use the search functionality to filter patients

### Creating a New Report
1. Click **Nuovo Referto** button
2. Select specialization (GP, Dermatologist, Cardiologist)
3. A Writer document opens with appropriate bookmarks
4. Fill in the medical content using native Writer features
5. Save using **Salva** button

### Saving to Database
1. Click **Salva** button when report is complete
2. Content is extracted from bookmarks
3. Sensitive data is encrypted with AES-GCM
4. SHA-256 hash is calculated for integrity
5. Record is saved to H2 database

### Exporting to PDF
1. Click **PDF** button
2. Document is exported using LibreOffice's native PDF exporter
3. No third-party libraries required

### Status Workflow
- **BOZZA**: Draft - can be modified
- **COMPLETATO**: Completed - ready for review
- **FIRMATO**: Signed - locked, cannot be modified
- **ARCHIVIATO**: Archived - final state

## Database Location

The H2 database is created in:
```
~/medical-reports/db/medical_reports.mv.db
```

## Bookmark Naming Convention

Templates must use these exact bookmark names:

### General Practitioner (template_gp.odt)
- `bm_anamnesi` - Anamnesis
- `bm_esame_obiettivo` - Objective Examination
- `bm_terapia` - Therapy
- `bm_diagnosi` - Diagnosis

### Dermatologist (template_dermatologo.odt)
- `bm_mappatura_lesioni` - Lesion Mapping
- `bm_dermoscopia` - Dermoscopy Findings
- `bm_diagnosi` - Diagnosis
- `bm_terapia` - Therapy

### Cardiologist (template_cardiologo.odt)
- `bm_dati_ecg` - ECG Data
- `bm_ecocardiogramma` - Echocardiogram
- `bm_fe` - Ejection Fraction (EF%)
- `bm_valvole` - Valves
- `bm_diagnosi` - Diagnosis
- `bm_terapia` - Therapy

## Configuration Files

### description.xml
Defines extension metadata, identifier, and registration.

### Addons.xcu
Configures toolbar buttons, menu items, and commands.

### protocolhandler.xcu
Registers custom protocol handlers for UNO dispatches.

## Development Notes

### UNO API Best Practices
- Always query interfaces using `UnoRuntime.queryInterface()`
- Handle `com.sun.star.uno.Exception` gracefully
- Dispose components when done to prevent memory leaks
- Use `XComponentContext` for all service creation

### Security Considerations
- Encryption key should be loaded from secure keystore in production
- Database file should have restricted file system permissions
- Audit logs should be immutable once written

### GDPR Compliance
- Personal data is obfuscated in UI (Calc dashboard)
- Only authorized users should have access to decryption keys
- Right to erasure requires secure key deletion

## Troubleshooting

### Extension Not Loading
1. Check LibreOffice version compatibility (4.0+)
2. Verify description.xml identifier is unique
3. Check console/log for error messages

### Database Connection Errors
1. Ensure H2 JAR is in classpath
2. Check file permissions on database directory
3. Verify encryption key is set before operations

### UNO Interface Errors
1. Ensure correct UNO JAR versions are used
2. Check LibreOffice is running in accepted mode
3. Verify component context is properly initialized

## License

This project is provided as-is for educational and demonstration purposes.

## Contributing

Contributions welcome! Please follow these guidelines:
- No Swing/JavaFX - use UNO dialogs only
- All text from properties files (i18n support)
- Comprehensive error handling
- Logging for all operations

## Support

For issues and feature requests, please open an issue on the project repository.
