package com.caresync.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync Database and Environment Configuration Tests")
class DatabaseConfigTest {

    @Test
    @DisplayName("Verify AppConstants default values")
    void testAppConstants() {
        assertEquals("PBKDF2WithHmacSHA256", AppConstants.PBKDF2_ALGORITHM);
        assertEquals(600_000, AppConstants.DEFAULT_PBKDF2_ITERATIONS, "Security baseline: 600,000 PBKDF2 iterations");
        assertEquals(16, AppConstants.SALT_BYTE_LENGTH);
        assertEquals(256, AppConstants.HASH_KEY_LENGTH_BITS);
        assertEquals("CARESYNC_SESSIONID", AppConstants.SESSION_COOKIE_NAME);
        assertEquals(30, AppConstants.DEFAULT_SESSION_TIMEOUT_MINUTES);
        assertEquals(30, AppConstants.DEFAULT_SLOT_DURATION_MINUTES);
        assertEquals(60, AppConstants.ADVANCE_BOOKING_MAX_DAYS);
        assertEquals(2, AppConstants.MINIMUM_CANCELLATION_HOURS);
    }

    @Test
    @DisplayName("Verify EnvConfig fallback and parsing logic")
    void testEnvConfigFallbacks() {
        String testVal = EnvConfig.get("NON_EXISTENT_VAR_12345", "fallback_default");
        assertEquals("fallback_default", testVal);

        int intVal = EnvConfig.getInt("NON_EXISTENT_INT_VAR", 42);
        assertEquals(42, intVal);

        boolean boolVal = EnvConfig.getBoolean("NON_EXISTENT_BOOL_VAR", true);
        assertTrue(boolVal);
    }

    @Test
    @DisplayName("Verify DatabaseConfig configuration detection does not crash")
    void testDatabaseConfigDetection() {
        // Without .env or environment credentials configured, isConfigured() safely returns false
        boolean configured = DatabaseConfig.isConfigured();
        assertFalse(configured, "In clean test environment without credentials, configured should be false");
    }
}
