package com.medicalreports.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security utility class providing AES-GCM encryption and SHA-256 hashing.
 * 
 * AES-GCM (Galois/Counter Mode) provides:
 * - Confidentiality (encryption)
 * - Integrity (authentication tag)
 * - Authenticated encryption with associated data (AEAD)
 * 
 * SHA-256 provides tamper-evidence for document content.
 * 
 * @author Medical Reports Team
 * @version 1.0.0
 */
public class SecurityUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String SHA256_ALGORITHM = "SHA-256";
    
    // GCM parameters
    private static final int GCM_IV_LENGTH = 12; // 96 bits recommended for GCM
    private static final int GCM_TAG_LENGTH = 128; // 128-bit authentication tag
    private static final int KEY_SIZE = 256; // AES-256

    /**
     * Generates a new AES-256 secret key.
     * 
     * @return SecretKey for AES encryption
     * @throws NoSuchAlgorithmException if algorithm is not available
     */
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    /**
     * Creates a SecretKey from a raw byte array.
     * 
     * @param keyData The raw key bytes
     * @return SecretKey object
     */
    public static SecretKey loadAESKey(byte[] keyData) {
        return new SecretKeySpec(keyData, 0, keyData.length, AES_ALGORITHM);
    }

    /**
     * Encrypts plaintext using AES-GCM with a random IV.
     * The IV is prepended to the ciphertext for storage/transmission.
     * 
     * Format: [IV (12 bytes)] + [Ciphertext] + [Authentication Tag (16 bytes)]
     * 
     * @param plaintext The data to encrypt
     * @param key The encryption key
     * @return Base64-encoded encrypted string
     * @throws Exception if encryption fails
     */
    public static String encryptAESGCM(String plaintext, SecretKey key) throws Exception {
        if (plaintext == null || plaintext.isEmpty()) {
            return "";
        }

        Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
        
        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        
        // Initialize cipher with IV
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        
        // Encrypt
        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = cipher.doFinal(plaintextBytes);
        
        // Combine IV and ciphertext (tag is included in ciphertext by Java)
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        
        // Return Base64 encoded
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decrypts ciphertext using AES-GCM.
     * Expects the IV to be prepended to the ciphertext.
     * 
     * @param encryptedDataBase64 Base64-encoded encrypted data
     * @param key The decryption key
     * @return Decrypted plaintext
     * @throws Exception if decryption fails (including authentication failure)
     */
    public static String decryptAESGCM(String encryptedDataBase64, SecretKey key) throws Exception {
        if (encryptedDataBase64 == null || encryptedDataBase64.isEmpty()) {
            return "";
        }

        // Decode Base64
        byte[] combined = Base64.getDecoder().decode(encryptedDataBase64);
        
        // Extract IV and ciphertext
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[combined.length - iv.length];
        
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);
        
        // Initialize cipher
        Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        
        // Decrypt
        byte[] plaintextBytes = cipher.doFinal(ciphertext);
        
        return new String(plaintextBytes, StandardCharsets.UTF_8);
    }

    /**
     * Calculates SHA-256 hash of the input text.
     * Used for tamper-evidence of document content.
     * 
     * @param text The text to hash
     * @return Hexadecimal representation of the hash
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    public static String calculateSHA256(String text) throws NoSuchAlgorithmException {
        if (text == null || text.isEmpty()) {
            return "";
        }

        MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
        byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        
        // Convert to hexadecimal
        StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }

    /**
     * Verifies that the text matches the expected SHA-256 hash.
     * 
     * @param text The text to verify
     * @param expectedHash The expected hash value
     * @return true if hash matches, false otherwise
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    public static boolean verifySHA256(String text, String expectedHash) throws NoSuchAlgorithmException {
        if (text == null || text.isEmpty()) {
            return expectedHash == null || expectedHash.isEmpty();
        }
        
        String calculatedHash = calculateSHA256(text);
        return calculatedHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * Concatenates multiple strings and calculates SHA-256 hash.
     * Useful for hashing document content from multiple bookmarks.
     * 
     * @param parts Strings to concatenate and hash
     * @return Hexadecimal representation of the hash
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    public static String calculateSHA256Concatenated(String... parts) throws NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null) {
                sb.append(part);
            }
        }
        return calculateSHA256(sb.toString());
    }

    /**
     * Generates a random salt for key derivation.
     * 
     * @param length Length of the salt in bytes
     * @return Random salt bytes
     */
    public static byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Encodes bytes to Base64 string.
     * 
     * @param data Bytes to encode
     * @return Base64-encoded string
     */
    public static String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Decodes Base64 string to bytes.
     * 
     * @param base64String Base64-encoded string
     * @return Decoded bytes
     */
    public static byte[] decodeBase64(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }
}
