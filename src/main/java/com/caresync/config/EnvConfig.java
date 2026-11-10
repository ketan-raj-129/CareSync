package com.caresync.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads configuration properties with precedence:
 * 1. JVM System Properties (-Dkey=value)
 * 2. OS Environment Variables (export KEY=VALUE)
 * 3. Local '.env' file in working directory
 */
public final class EnvConfig {

    private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);
    private static final Map<String, String> ENV_VARS = new HashMap<>();

    static {
        loadDotEnv();
    }

    private EnvConfig() {}

    private static void loadDotEnv() {
        File dotEnv = new File(".env");
        if (!dotEnv.exists() || !dotEnv.isFile()) {
            logger.debug(".env file not found in current directory; relying on OS environment variables.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(dotEnv, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();
                    // Strip optional enclosing quotes
                    if ((value.startsWith("\"") && value.endsWith("\"")) || 
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    ENV_VARS.put(key, value);
                }
            }
            logger.info("Loaded configuration parameters from local .env file.");
        } catch (IOException ex) {
            logger.warn("Could not read .env file: {}", ex.getMessage());
        }
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        String envVar = System.getenv(key);
        if (envVar != null && !envVar.trim().isEmpty()) {
            return envVar.trim();
        }
        String fileVar = ENV_VARS.get(key);
        if (fileVar != null && !fileVar.trim().isEmpty()) {
            return fileVar.trim();
        }
        return defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer for config key '{}': '{}'. Using default: {}", key, val, defaultValue);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(val);
    }
}

