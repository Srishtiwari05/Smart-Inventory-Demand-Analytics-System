package com.inventory.services;

import com.inventory.models.User;
import com.inventory.utils.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private final AuthService authService = AuthService.getInstance();

    @Test
    @DisplayName("Password hashing produces consistent SHA-256 hex string")
    void testPasswordHashing() {
        String hash1 = SecurityUtil.hashPassword("admin123");
        String hash2 = SecurityUtil.hashPassword("admin123");
        assertNotNull(hash1);
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2);

        // Different passwords must produce different hashes
        String hash3 = SecurityUtil.hashPassword("secret456");
        assertNotEquals(hash1, hash3);
    }

    @Test
    @DisplayName("Salted password hashing generates unique salts and verifiable hashes")
    void testSaltedPasswordHashing() {
        String salt1 = SecurityUtil.generateSalt();
        String salt2 = SecurityUtil.generateSalt();
        assertNotNull(salt1);
        assertNotNull(salt2);
        assertNotEquals(salt1, salt2);

        String saltedHash1 = SecurityUtil.hashPasswordWithSalt("admin123", salt1);
        String saltedHash2 = SecurityUtil.hashPasswordWithSalt("admin123", salt2);
        assertNotEquals(saltedHash1, saltedHash2);
    }

    @Test
    @DisplayName("Password strength policy enforces length and digit/letter criteria")
    void testPasswordStrengthValidation() {
        // Valid passwords (>= 8 chars, has digit + letter)
        assertTrue(SecurityUtil.validatePasswordStrength("Passw0rd123"));
        assertTrue(SecurityUtil.validatePasswordStrength("admin1234"));
        assertTrue(SecurityUtil.validatePasswordStrength("secure89pass"));

        // Invalid passwords
        assertFalse(SecurityUtil.validatePasswordStrength("short1")); // < 8 chars
        assertFalse(SecurityUtil.validatePasswordStrength("onlyletters")); // no digit
        assertFalse(SecurityUtil.validatePasswordStrength("1234567890")); // no letter
        assertFalse(SecurityUtil.validatePasswordStrength(null)); // null
        assertFalse(SecurityUtil.validatePasswordStrength("")); // empty
    }

    @Test
    @DisplayName("UserSession expiration and purge logic behaves correctly")
    void testUserSessionExpiration() {
        User user = new User("testuser", "pass", User.Role.STAFF);
        
        // Active session with 1 hour TTL
        AuthService.UserSession activeSession = new AuthService.UserSession(user, 3600_000L);
        assertFalse(activeSession.isExpired());
        assertEquals(user, activeSession.getUser());

        // Expired session with negative TTL
        AuthService.UserSession expiredSession = new AuthService.UserSession(user, -1000L);
        assertTrue(expiredSession.isExpired());
    }

    @Test
    @DisplayName("Role hierarchy & permission checks correctly gate features")
    void testRolePermissions() {
        User owner = new User("owner", "pass", User.Role.OWNER);
        User manager = new User("mgr", "pass", User.Role.MANAGER);
        User staff = new User("staff", "pass", User.Role.STAFF);
        User supplier = new User("supplier", "pass", User.Role.SUPPLIER);

        // OWNER permissions
        assertTrue(authService.hasPermission(owner, "API_VIEW_PRODUCTS"));
        assertTrue(authService.hasPermission(owner, "API_ADD_PRODUCT"));
        assertTrue(authService.hasPermission(owner, "API_DELETE_PRODUCT"));
        assertTrue(authService.hasPermission(owner, "API_MANAGE_USERS"));
        assertTrue(authService.hasPermission(owner, "API_VIEW_ANALYTICS"));
        assertTrue(authService.hasPermission(owner, "API_VIEW_AUDIT_LOGS"));

        // MANAGER permissions
        assertTrue(authService.hasPermission(manager, "API_VIEW_PRODUCTS"));
        assertTrue(authService.hasPermission(manager, "API_ADD_PRODUCT"));
        assertTrue(authService.hasPermission(manager, "API_VIEW_ANALYTICS"));
        assertTrue(authService.hasPermission(manager, "API_APPROVE_PR"));
        assertFalse(authService.hasPermission(manager, "API_DELETE_PRODUCT"));
        assertFalse(authService.hasPermission(manager, "API_MANAGE_USERS"));

        // STAFF permissions (read / order only)
        assertTrue(authService.hasPermission(staff, "API_VIEW_PRODUCTS"));
        assertTrue(authService.hasPermission(staff, "API_CREATE_PR"));
        assertFalse(authService.hasPermission(staff, "API_ADD_PRODUCT"));
        assertFalse(authService.hasPermission(staff, "API_APPROVE_PR"));
        assertFalse(authService.hasPermission(staff, "API_VIEW_ANALYTICS"));
        assertFalse(authService.hasPermission(staff, "API_MANAGE_USERS"));

        // SUPPLIER permissions
        assertTrue(authService.hasPermission(supplier, "API_SUPPLIER_PORTAL"));
        assertFalse(authService.hasPermission(supplier, "API_VIEW_PRODUCTS"));
        assertFalse(authService.hasPermission(supplier, "API_VIEW_ANALYTICS"));
        assertFalse(authService.hasPermission(supplier, "API_MANAGE_USERS"));

        // Null user
        assertFalse(authService.hasPermission(null, "API_VIEW_PRODUCTS"));
    }

    @Test
    @DisplayName("Token generation creates valid UUID format")
    void testTokenGeneration() {
        String token = SecurityUtil.generateToken();
        assertNotNull(token);
        assertEquals(36, token.length());
        assertTrue(token.matches("^[0-9a-fA-F-]{36}$"));
    }
}
