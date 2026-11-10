package com.caresync.util;

import com.caresync.config.AppConstants;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * Cryptographic utility for password hashing and verification using PBKDF2-HMAC-SHA256.
 * Enforces production security baseline of 600,000 iterations, 16-byte cryptographically
 * secure salt, and 256-bit derived key length.
 */
public class PasswordHasher {

    private final int defaultIterations;
    private final SecureRandom secureRandom;

    /**
     * Production constructor enforcing 600,000 iterations baseline.
     */
    public PasswordHasher() {
        this(AppConstants.DEFAULT_PBKDF2_ITERATIONS);
    }

    /**
     * Parameterized constructor permitting explicit iteration count configuration for testing.
     *
     * @param defaultIterations iteration count (must be >= 1000)
     */
    public PasswordHasher(int defaultIterations) {
        if (defaultIterations < 1000) {
            throw new IllegalArgumentException("Iterations must be at least 1,000");
        }
        this.defaultIterations = defaultIterations;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generates a new unique salt and hashes the plaintext password using default iterations.
     *
     * @param password password character array
     * @return HashResult containing hex-encoded hash, salt, and iteration count
     */
    public HashResult hashPassword(char[] password) {
        byte[] salt = new byte[AppConstants.SALT_BYTE_LENGTH];
        secureRandom.nextBytes(salt);
        return hashPassword(password, salt, this.defaultIterations);
    }

    /**
     * Hashes the plaintext password with the provided salt and iteration count.
     *
     * @param password   password character array
     * @param salt       cryptographic salt bytes
     * @param iterations PBKDF2 iteration count
     * @return HashResult containing hex-encoded hash, salt, and iteration count
     */
    public HashResult hashPassword(char[] password, byte[] salt, int iterations) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (salt == null || salt.length == 0) {
            throw new IllegalArgumentException("Salt cannot be empty");
        }
        if (iterations < 1000) {
            throw new IllegalArgumentException("Iterations must be at least 1,000");
        }

        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, AppConstants.HASH_KEY_LENGTH_BITS);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(AppConstants.PBKDF2_ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();

            String hashHex = HexFormat.of().formatHex(hash);
            String saltHex = HexFormat.of().formatHex(salt);

            return new HashResult(hashHex, saltHex, iterations);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Failed to derive PBKDF2 hash", e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Verifies a candidate password against stored hash, salt, and iteration metadata.
     * Performs constant-time comparison to prevent timing attacks.
     *
     * @param candidatePassword password character array supplied by user
     * @param storedHashHex     stored hex-encoded derived hash
     * @param storedSaltHex     stored hex-encoded salt
     * @param storedIterations  iteration count used when hash was generated
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(char[] candidatePassword, String storedHashHex, String storedSaltHex, int storedIterations) {
        if (candidatePassword == null || candidatePassword.length == 0 ||
            storedHashHex == null || storedSaltHex == null || storedIterations < 1000) {
            return false;
        }

        byte[] salt;
        byte[] expectedHash;
        try {
            salt = HexFormat.of().parseHex(storedSaltHex);
            expectedHash = HexFormat.of().parseHex(storedHashHex);
        } catch (IllegalArgumentException e) {
            return false;
        }

        PBEKeySpec spec = new PBEKeySpec(candidatePassword, salt, storedIterations, AppConstants.HASH_KEY_LENGTH_BITS);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(AppConstants.PBKDF2_ALGORITHM);
            byte[] computedHash = skf.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(expectedHash, computedHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return false;
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Determines whether the stored work factor is below the current baseline and requires rehashing.
     *
     * @param storedIterations iteration count of stored credential
     * @return true if rehash is recommended, false otherwise
     */
    public boolean needsRehash(int storedIterations) {
        return storedIterations < this.defaultIterations;
    }

    public int getDefaultIterations() {
        return defaultIterations;
    }

    /**
     * Container record for PBKDF2 hash output.
     */
    public record HashResult(String hashHex, String saltHex, int iterations) {}
}

