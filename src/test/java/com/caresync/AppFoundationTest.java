package com.caresync;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CareSync Phase 1 Foundation Tests")
class AppFoundationTest {

    @Test
    @DisplayName("Verify Java 21 runtime environment")
    void testJavaRuntimeVersion() {
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion, "Java version should not be null");
        assertTrue(javaVersion.startsWith("21"), "Should be executing on Java 21 LTS runtime");
    }

    @Test
    @DisplayName("Verify CareSync package namespace presence")
    void testPackageNamespace() {
        Package pkg = AppFoundationTest.class.getPackage();
        assertNotNull(pkg, "Package must be defined");
        assertTrue(pkg.getName().startsWith("com.caresync"), "Package name should conform to com.caresync.*");
    }
}

