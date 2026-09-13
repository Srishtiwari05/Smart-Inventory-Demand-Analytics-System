package com.inventory.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

public class SecurityUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hashes a plain-text password using legacy SHA-256.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Generates a random cryptographic 16-byte hex salt.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * Hashes a plain-text password with a cryptographic salt using SHA-256.
     */
    public static String hashPasswordWithSalt(String password, String salt) {
        if (salt == null || salt.isBlank()) {
            return hashPassword(password);
        }
        return hashPassword(password + ":" + salt);
    }

    /**
     * Validates password strength policy:
     * - Minimum 8 characters long
     * - Contains at least one digit
     * - Contains at least one letter
     */
    public static boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasDigit = false;
        boolean hasLetter = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            if (Character.isLetter(c)) hasLetter = true;
            if (hasDigit && hasLetter) return true;
        }
        return false;
    }

    /**
     * Generates a random UUID token for API sessions.
     */
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
