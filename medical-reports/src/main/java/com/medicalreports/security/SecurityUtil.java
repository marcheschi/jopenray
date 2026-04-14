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
 * Security utilities for AES-GCM encryption and SHA-256 hashing.
 */
public class SecurityUtil {

    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String SHA256_ALGORITHM = "SHA-256";

    private final SecretKey secretKey;

    /**
     * Creates a new SecurityUtil with a generated key.
     * In production, the key should be loaded from a secure keystore.
     */
    public SecurityUtil() throws Exception {
        this.secretKey = generateKey();
    }

    /**
     * Creates a SecurityUtil with a specific key (for testing or key loading).
     */
    public SecurityUtil(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Generates a new AES-256 key.
     */
    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(256, new SecureRandom());
        return keyGen.generateKey();
    }

    /**
     * Encrypts plaintext using AES-GCM.
     * Returns Base64 encoded string containing IV + ciphertext.
     */
    public String encrypt(String plaintext) throws Exception {
        if (plaintext == null || plaintext.isEmpty()) {
            return "";
        }

        Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
        
        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
        
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        // Combine IV and ciphertext
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decrypts ciphertext using AES-GCM.
     * Expects Base64 encoded string containing IV + ciphertext.
     */
    public String decrypt(String encryptedData) throws Exception {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return "";
        }

        byte[] combined = Base64.getDecoder().decode(encryptedData);
        
        // Extract IV and ciphertext
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[combined.length - iv.length];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);
        
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        
        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    /**
     * Calculates SHA-256 hash of the input text.
     */
    public static String calculateSHA256(String input) throws NoSuchAlgorithmException {
        if (input == null || input.isEmpty()) {
            return "";
        }

        MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        
        // Convert to hex string
        StringBuilder hexString = new StringBuilder();
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
     * Verifies if the content matches the expected hash.
     */
    public static boolean verifyHash(String content, String expectedHash) throws NoSuchAlgorithmException {
        String calculatedHash = calculateSHA256(content);
        return calculatedHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * Gets the secret key (for serialization or storage).
     */
    public SecretKey getSecretKey() {
        return secretKey;
    }

    /**
     * Creates a SecretKey from a byte array.
     */
    public static SecretKey createKeyFromBytes(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, AES_ALGORITHM);
    }
}
