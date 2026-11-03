package com.ecommerce.test;

import java.util.Objects;

/**
 * Lightweight assertion utility providing test verification without external test runners.
 * 
 * @author baktiyarovamereilim-lang
 */
public final class SimpleAssert {
    private SimpleAssert() {}

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion Failed (expected false): " + message);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(String.format("Assertion Failed: %s [Expected: %s, Actual: %s]", message, expected, actual));
        }
    }

    public static void assertEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(String.format("Assertion Failed: %s [Expected: %.4f, Actual: %.4f, Delta: %.4f]",
                    message, expected, actual, delta));
        }
    }

    public static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError("Assertion Failed: " + message + " (Object is null)");
        }
    }
}
