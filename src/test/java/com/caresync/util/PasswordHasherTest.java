package com.caresync.util;

import com.caresync.config.AppConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordHasher Security and PBKDF2-HMAC-SHA256 Tests")
class PasswordHasherTest {

    @Test
    @DisplayName("Verify production baseline work factor is 600,000 iterations")
    void testProductionBaselineIterations() {
        PasswordHasher prodHasher = new PasswordHasher();
        assertEquals(AppConstants.DEFAULT_PBKDF2_ITERATIONS, prodHasher.getDefaultIterations());
        assertEquals(600_000, prodHasher.getDefaultIterations(), "Production baseline must be 600,000 iterations");
    }

    @Test
    @DisplayName("Verify password hashing produces unique secure salts and correct lengths")
    void testPasswordHashing() {
        // Use 10,000 iterations for fast test execution
        PasswordHasher hasher = new PasswordHasher(10_000);

        char[] pass1 = "SecurePass123!".toCharArray();
        PasswordHasher.HashResult result1 = hasher.hashPassword(pass1);

        assertNotNull(result1.hashHex());
        assertNotNull(result1.saltHex());
        assertEquals(10_000, result1.iterations());

        // Salt should be 16 bytes = 32 hex chars
        assertEquals(32, result1.saltHex().length(), "16-byte salt must be 32 hex characters");
        // Hash should be 256 bits (32 bytes) = 64 hex chars
        assertEquals(64, result1.hashHex().length(), "256-bit derived key must be 64 hex characters");

        // Independent hash of same password must generate different salt and hash
        PasswordHasher.HashResult result2 = hasher.hashPassword(pass1);
        assertNotEquals(result1.saltHex(), result2.saltHex(), "Each hash operation must use a fresh random salt");
        assertNotEquals(result1.hashHex(), result2.hashHex(), "Hashes with different salts must differ");
    }

    @Test
    @DisplayName("Verify password verification with correct and incorrect passwords")
    void testPasswordVerification() {
        PasswordHasher hasher = new PasswordHasher(10_000);
        char[] password = "MySecretPassword#2026".toCharArray();

        PasswordHasher.HashResult hashResult = hasher.hashPassword(password);

        // Correct password should verify
        assertTrue(hasher.verifyPassword(
                "MySecretPassword#2026".toCharArray(),
                hashResult.hashHex(),
                hashResult.saltHex(),
                hashResult.iterations()
        ), "Correct password must verify successfully");

        // Incorrect password should fail
        assertFalse(hasher.verifyPassword(
                "WrongPassword#2026".toCharArray(),
                hashResult.hashHex(),
                hashResult.saltHex(),
                hashResult.iterations()
        ), "Incorrect password must be rejected");

        // Case difference should fail
        assertFalse(hasher.verifyPassword(
                "mysecretpassword#2026".toCharArray(),
                hashResult.hashHex(),
                hashResult.saltHex(),
                hashResult.iterations()
        ), "Case altered password must be rejected");
    }

    @Test
    @DisplayName("Verify invalid metadata and malformed inputs are safely rejected")
    void testInvalidMetadataHandling() {
        PasswordHasher hasher = new PasswordHasher(10_000);

        assertFalse(hasher.verifyPassword(null, "aabb", "ccdd", 10_000));
        assertFalse(hasher.verifyPassword("pass".toCharArray(), null, "ccdd", 10_000));
        assertFalse(hasher.verifyPassword("pass".toCharArray(), "aabb", null, 10_000));
        assertFalse(hasher.verifyPassword("pass".toCharArray(), "invalid_non_hex", "invalid_non_hex", 10_000));
        assertFalse(hasher.verifyPassword("pass".toCharArray(), "aabb", "ccdd", 500), "Iterations < 1000 must be rejected");
    }

    @Test
    @DisplayName("Verify work factor rehash detection logic")
    void testNeedsRehashDetection() {
        PasswordHasher hasher = new PasswordHasher(600_000);

        // Old legacy iteration work factors should require upgrade
        assertTrue(hasher.needsRehash(65_536), "65,536 iterations must trigger rehash upgrade");
        assertTrue(hasher.needsRehash(100_000), "100,000 iterations must trigger rehash upgrade");
        assertTrue(hasher.needsRehash(599_999), "599,999 iterations must trigger rehash upgrade");

        // Current or higher work factors do not require rehash
        assertFalse(hasher.needsRehash(600_000), "600,000 iterations is baseline, no rehash required");
        assertFalse(hasher.needsRehash(700_000), "700,000 iterations exceeds baseline, no rehash required");
    }
}

