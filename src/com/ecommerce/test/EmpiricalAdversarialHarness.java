package com.ecommerce.test;

import com.ecommerce.decorator.GiftWrapDecorator;
import com.ecommerce.decorator.OrderItem;
import com.ecommerce.decorator.StandardOrderItem;
import com.ecommerce.frontend.EcommerceSwingApp;
import com.ecommerce.frontend.FrontendInputValidator;
import com.ecommerce.frontend.FrontendInputValidator.ValidationResult;
import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.ProductType;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Empirical Adversarial Test Harness for Challenger 1 (Milestone 1).
 * Stress tests FrontendInputValidator against boundary inputs, integer overflow,
 * combinatorial edge cases, injection attempts, format anomalies, and Swing UI event handlers.
 */
public class EmpiricalAdversarialHarness {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("CHALLENGER 1: EMPIRICAL ADVERSARIAL STRESS TEST HARNESS");
        System.out.println("Target: com.ecommerce.frontend.FrontendInputValidator & EcommerceSwingApp UI");
        System.out.println("================================================================================\n");

        runQuantityBoundarySuite();
        runCustomRigBoundarySuite();
        runPromoCodeBoundarySuite();
        runStripeCredentialsSuite();
        runPayPalCredentialsSuite();
        runCryptoCredentialsSuite();
        runLuhnChecksumExhaustiveSuite();
        runUiEventIntegrationSuite();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.printf("CHALLENGE RESULTS: %d Total, %d Passed, %d Failed%n",
                totalTests, passedTests, failedTests);
        System.out.println("================================================================================");

        if (failedTests > 0) {
            System.err.println("\n[FAILURES DETECTED]:");
            for (String f : failures) {
                System.err.println("  - " + f);
            }
            System.exit(1);
        } else {
            System.out.println("\n[VERDICT]: ALL EMPIRICAL CHALLENGES PASSED (100% SUCCESS)");
        }
    }

    private static void check(String testName, boolean condition, String failureDetails) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            failedTests++;
            String msg = testName + " -> " + failureDetails;
            failures.add(msg);
            System.err.println("  [FAIL] " + msg);
        }
    }

    // =========================================================================
    // 1. QUANTITY BOUNDARY & ARITHMETIC OVERFLOW STRESS TESTS
    // =========================================================================
    private static void runQuantityBoundarySuite() {
        System.out.println("[SUITE 1] Quantity Boundaries & Arithmetic Overflow");

        // Requested quantity <= 0
        ValidationResult rZero = FrontendInputValidator.validateCatalogQuantity(0, 0, 10);
        check("Quantity = 0 rejected", !rZero.isValid(), rZero.toString());

        ValidationResult rNeg1 = FrontendInputValidator.validateCatalogQuantity(-1, 0, 10);
        check("Quantity = -1 rejected", !rNeg1.isValid(), rNeg1.toString());

        ValidationResult rMinInt = FrontendInputValidator.validateCatalogQuantity(Integer.MIN_VALUE, 0, 10);
        check("Quantity = Integer.MIN_VALUE rejected", !rMinInt.isValid(), rMinInt.toString());

        // Available stock <= 0
        ValidationResult rStockZero = FrontendInputValidator.validateCatalogQuantity(1, 0, 0);
        check("Available stock = 0 rejected", !rStockZero.isValid(), rStockZero.toString());

        ValidationResult rStockNeg = FrontendInputValidator.validateCatalogQuantity(1, 0, -5);
        check("Available stock = -5 rejected", !rStockNeg.isValid(), rStockNeg.toString());

        ValidationResult rStockMinInt = FrontendInputValidator.validateCatalogQuantity(1, 0, Integer.MIN_VALUE);
        check("Available stock = Integer.MIN_VALUE rejected", !rStockMinInt.isValid(), rStockMinInt.toString());

        // Exact stock limits
        ValidationResult rExact = FrontendInputValidator.validateCatalogQuantity(10, 0, 10);
        check("Exact stock limit (10 requested, 10 available, 0 in cart) accepted", rExact.isValid(), rExact.toString());

        ValidationResult rOverByOne = FrontendInputValidator.validateCatalogQuantity(11, 0, 10);
        check("Over stock by 1 (11 requested, 10 available, 0 in cart) rejected", !rOverByOne.isValid(), rOverByOne.toString());

        // Cumulative cart allocation
        ValidationResult rCumulExact = FrontendInputValidator.validateCatalogQuantity(4, 6, 10);
        check("Cumulative exact stock (4 requested + 6 in cart = 10 available) accepted", rCumulExact.isValid(), rCumulExact.toString());

        ValidationResult rCumulOver = FrontendInputValidator.validateCatalogQuantity(5, 6, 10);
        check("Cumulative overflow (5 requested + 6 in cart = 11 > 10 available) rejected", !rCumulOver.isValid(), rCumulOver.toString());

        // Negative in-cart values (gracefully clamped to 0)
        ValidationResult rNegCartValid = FrontendInputValidator.validateCatalogQuantity(10, -5, 10);
        check("Negative inCart clamped to 0 (10 requested, -5 in cart, 10 available) accepted", rNegCartValid.isValid(), rNegCartValid.toString());

        ValidationResult rNegCartOver = FrontendInputValidator.validateCatalogQuantity(11, -5, 10);
        check("Negative inCart over stock (11 requested, -5 in cart, 10 available) rejected", !rNegCartOver.isValid(), rNegCartOver.toString());

        // 32-bit Integer Overflow resistance
        // (long) cartAlloc + (long) requestedQty must not overflow into negative
        ValidationResult rOverflow1 = FrontendInputValidator.validateCatalogQuantity(Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
        check("Integer overflow check (MAX_VALUE requested + 1 in cart > MAX_VALUE stock) rejected", !rOverflow1.isValid(), rOverflow1.toString());

        ValidationResult rOverflow2 = FrontendInputValidator.validateCatalogQuantity(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        check("Double MAX_VALUE overflow check rejected", !rOverflow2.isValid(), rOverflow2.toString());

        ValidationResult rOverflowStock = FrontendInputValidator.validateCatalogQuantity(Integer.MAX_VALUE, 0, 5);
        check("MAX_VALUE requested on 5 stock rejected", !rOverflowStock.isValid(), rOverflowStock.toString());
    }

    // =========================================================================
    // 2. CUSTOM PC BUILDER RIG ASSEMBLY BOUNDARY TESTS
    // =========================================================================
    private static void runCustomRigBoundarySuite() {
        System.out.println("\n[SUITE 2] Custom PC Builder Rig Assembly Boundaries");

        Object cpu = new Object();
        Object ram = new Object();
        Object gpu = new Object();
        Object ssd = new Object();
        Object os = new Object();

        // Rig Name Null / Empty / Whitespace
        ValidationResult rNullName = FrontendInputValidator.validateCustomRig(null, cpu, ram, gpu, ssd, os);
        check("Null rig name rejected", !rNullName.isValid(), rNullName.toString());

        ValidationResult rEmptyName = FrontendInputValidator.validateCustomRig("", cpu, ram, gpu, ssd, os);
        check("Empty rig name rejected", !rEmptyName.isValid(), rEmptyName.toString());

        ValidationResult rSpacesName = FrontendInputValidator.validateCustomRig("     ", cpu, ram, gpu, ssd, os);
        check("Whitespace-only rig name rejected", !rSpacesName.isValid(), rSpacesName.toString());

        ValidationResult rTabsName = FrontendInputValidator.validateCustomRig("\t\n\r  ", cpu, ram, gpu, ssd, os);
        check("Tabs/newlines-only rig name rejected", !rTabsName.isValid(), rTabsName.toString());

        // Rig Name Length boundaries
        ValidationResult r1Char = FrontendInputValidator.validateCustomRig("X", cpu, ram, gpu, ssd, os);
        check("1-char rig name accepted", r1Char.isValid(), r1Char.toString());

        String name50 = "A".repeat(50);
        ValidationResult r50Chars = FrontendInputValidator.validateCustomRig(name50, cpu, ram, gpu, ssd, os);
        check("Exact 50-char rig name accepted", r50Chars.isValid(), r50Chars.toString());

        String name51 = "A".repeat(51);
        ValidationResult r51Chars = FrontendInputValidator.validateCustomRig(name51, cpu, ram, gpu, ssd, os);
        check("51-char rig name rejected", !r51Chars.isValid(), r51Chars.toString());

        String name50WithSpaces = "   " + "B".repeat(50) + "   ";
        ValidationResult r50Trimmed = FrontendInputValidator.validateCustomRig(name50WithSpaces, cpu, ram, gpu, ssd, os);
        check("50-char rig name with outer whitespace accepted after trim", r50Trimmed.isValid(), r50Trimmed.toString());

        String name51WithSpaces = "   " + "B".repeat(51) + "   ";
        ValidationResult r51Trimmed = FrontendInputValidator.validateCustomRig(name51WithSpaces, cpu, ram, gpu, ssd, os);
        check("51-char rig name with outer whitespace rejected after trim", !r51Trimmed.isValid(), r51Trimmed.toString());

        // Special characters and Unicode in rig name
        ValidationResult rSpecial = FrontendInputValidator.validateCustomRig("Rig #1 (Super-Gamer & Studio) [2026]!", cpu, ram, gpu, ssd, os);
        check("Special characters in rig name accepted", rSpecial.isValid(), rSpecial.toString());

        ValidationResult rUnicode = FrontendInputValidator.validateCustomRig("Кастомная Ретро Сборка 90-х", cpu, ram, gpu, ssd, os);
        check("Unicode Cyrillic characters in rig name accepted", rUnicode.isValid(), rUnicode.toString());

        // Component Option Nullability
        ValidationResult rNullCpu = FrontendInputValidator.validateCustomRig("Valid Rig", null, ram, gpu, ssd, os);
        check("Null CPU rejected", !rNullCpu.isValid(), rNullCpu.toString());

        ValidationResult rNullRam = FrontendInputValidator.validateCustomRig("Valid Rig", cpu, null, gpu, ssd, os);
        check("Null RAM rejected", !rNullRam.isValid(), rNullRam.toString());

        ValidationResult rNullGpu = FrontendInputValidator.validateCustomRig("Valid Rig", cpu, ram, null, ssd, os);
        check("Null GPU rejected", !rNullGpu.isValid(), rNullGpu.toString());

        ValidationResult rNullSsd = FrontendInputValidator.validateCustomRig("Valid Rig", cpu, ram, gpu, null, os);
        check("Null SSD rejected", !rNullSsd.isValid(), rNullSsd.toString());

        ValidationResult rNullOs = FrontendInputValidator.validateCustomRig("Valid Rig", cpu, ram, gpu, ssd, null);
        check("Null OS rejected", !rNullOs.isValid(), rNullOs.toString());

        ValidationResult rAllNull = FrontendInputValidator.validateCustomRig("Valid Rig", null, null, null, null, null);
        check("All null components rejected", !rAllNull.isValid(), rAllNull.toString());
    }

    // =========================================================================
    // 3. PROMO CODE EDGE CASES & SPEND THRESHOLDS
    // =========================================================================
    private static void runPromoCodeBoundarySuite() {
        System.out.println("\n[SUITE 3] Promotional Code Boundaries & Spend Thresholds");

        // Null / Empty / Blank codes
        check("Null promo code rejected", !FrontendInputValidator.validatePromoCode(null, 100.0).isValid(), "null accepted");
        check("Empty promo code rejected", !FrontendInputValidator.validatePromoCode("", 100.0).isValid(), "empty accepted");
        check("Whitespace promo code rejected", !FrontendInputValidator.validatePromoCode("   ", 100.0).isValid(), "whitespace accepted");

        // Case insensitivity & trimming
        check("Lowercase 'save10' accepted", FrontendInputValidator.validatePromoCode("save10", 50.0).isValid(), "save10 failed");
        check("Mixed case 'FaLl15' accepted", FrontendInputValidator.validatePromoCode("FaLl15", 50.0).isValid(), "FaLl15 failed");
        check("Lowercase 'bogo' accepted", FrontendInputValidator.validatePromoCode("bogo", 50.0).isValid(), "bogo failed");
        check("Lowercase 'loyalty' accepted", FrontendInputValidator.validatePromoCode("loyalty", 50.0).isValid(), "loyalty failed");
        check("Lowercase 'none' accepted", FrontendInputValidator.validatePromoCode("none", 50.0).isValid(), "none failed");
        check("Surrounding whitespace '  SAVE10  ' accepted", FrontendInputValidator.validatePromoCode("  SAVE10  ", 50.0).isValid(), "padded save10 failed");

        // Unrecognized codes
        check("Code 'SAVE20' rejected", !FrontendInputValidator.validatePromoCode("SAVE20", 100.0).isValid(), "SAVE20 accepted");
        check("Code 'UNKNOWN' rejected", !FrontendInputValidator.validatePromoCode("UNKNOWN", 500.0).isValid(), "UNKNOWN accepted");
        check("SQL injection attempt code rejected", !FrontendInputValidator.validatePromoCode("' OR 1=1 --", 500.0).isValid(), "SQL injection accepted");

        // SAVE50 Threshold Boundaries ($250.00 exact limit)
        check("SAVE50 at $0.00 subtotal rejected", !FrontendInputValidator.validatePromoCode("SAVE50", 0.00).isValid(), "0.00 accepted");
        check("SAVE50 at negative subtotal -$10.00 rejected", !FrontendInputValidator.validatePromoCode("SAVE50", -10.00).isValid(), "negative subtotal accepted");
        check("SAVE50 at $249.00 subtotal rejected", !FrontendInputValidator.validatePromoCode("SAVE50", 249.00).isValid(), "249.00 accepted");
        check("SAVE50 at $249.99 subtotal rejected", !FrontendInputValidator.validatePromoCode("SAVE50", 249.99).isValid(), "249.99 accepted");
        check("SAVE50 at $249.999 subtotal rejected", !FrontendInputValidator.validatePromoCode("SAVE50", 249.999).isValid(), "249.999 accepted");
        check("SAVE50 at exact $250.00 subtotal accepted", FrontendInputValidator.validatePromoCode("SAVE50", 250.00).isValid(), "250.00 failed");
        check("SAVE50 at $250.01 subtotal accepted", FrontendInputValidator.validatePromoCode("SAVE50", 250.01).isValid(), "250.01 failed");
        check("SAVE50 at $1,000.00 subtotal accepted", FrontendInputValidator.validatePromoCode("SAVE50", 1000.00).isValid(), "1000.00 failed");
        check("SAVE50 lowercase 'save50' at $250.00 accepted", FrontendInputValidator.validatePromoCode("save50", 250.00).isValid(), "lowercase save50 failed");
    }

    // =========================================================================
    // 4. STRIPE PAYMENT CREDENTIALS & CARD EXPIRY STRESS TESTS
    // =========================================================================
    private static void runStripeCredentialsSuite() {
        System.out.println("\n[SUITE 4] Stripe Payment Credentials & Card Expiry");

        // Cardholder Name boundaries
        check("Null cardholder rejected",
                !FrontendInputValidator.validateStripeCredentials(null, "4242424242424242", "12/28", "123").isValid(), "null accepted");
        check("Empty cardholder rejected",
                !FrontendInputValidator.validateStripeCredentials("", "4242424242424242", "12/28", "123").isValid(), "empty accepted");
        check("1-char cardholder rejected (min 2)",
                !FrontendInputValidator.validateStripeCredentials("A", "4242424242424242", "12/28", "123").isValid(), "1 char accepted");
        check("2-char cardholder accepted",
                FrontendInputValidator.validateStripeCredentials("Al", "4242424242424242", "12/28", "123").isValid(), "2 char failed");
        check("70-char cardholder accepted",
                FrontendInputValidator.validateStripeCredentials("A".repeat(70), "4242424242424242", "12/28", "123").isValid(), "70 char failed");
        check("71-char cardholder rejected (max 70)",
                !FrontendInputValidator.validateStripeCredentials("A".repeat(71), "4242424242424242", "12/28", "123").isValid(), "71 char accepted");

        // Cardholder special character validity
        check("Hyphenated cardholder 'Mary-Jane Watson' accepted",
                FrontendInputValidator.validateStripeCredentials("Mary-Jane Watson", "4242424242424242", "12/28", "123").isValid(), "hyphen failed");
        check("Apostrophe in cardholder 'Conan O'Brien' accepted",
                FrontendInputValidator.validateStripeCredentials("Conan O'Brien", "4242424242424242", "12/28", "123").isValid(), "apostrophe failed");
        check("Periods in cardholder 'Dr. John H. Watson' accepted",
                FrontendInputValidator.validateStripeCredentials("Dr. John H. Watson", "4242424242424242", "12/28", "123").isValid(), "dots failed");
        check("Numbers in cardholder 'John 3rd' rejected",
                !FrontendInputValidator.validateStripeCredentials("John 3rd", "4242424242424242", "12/28", "123").isValid(), "numbers accepted");
        check("Script tag in cardholder '<script>' rejected",
                !FrontendInputValidator.validateStripeCredentials("<script>", "4242424242424242", "12/28", "123").isValid(), "script tag accepted");

        // Card Number length & formatting
        check("15-digit card rejected",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "424242424242424", "12/28", "123").isValid(), "15 digits accepted");
        check("17-digit card rejected",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "42424242424242421", "12/28", "123").isValid(), "17 digits accepted");
        check("16-digit card with alphabetic char rejected",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "424242424242424A", "12/28", "123").isValid(), "letters in card accepted");
        check("Spaces in card '4242 4242 4242 4242' accepted",
                FrontendInputValidator.validateStripeCredentials("John Doe", "4242 4242 4242 4242", "12/28", "123").isValid(), "spaced card failed");
        check("Hyphens in card '4242-4242-4242-4242' accepted",
                FrontendInputValidator.validateStripeCredentials("John Doe", "4242-4242-4242-4242", "12/28", "123").isValid(), "hyphenated card failed");

        // Expiration MM/YY formatting & date math
        YearMonth now = YearMonth.now();
        String currentMonthStr = String.format("%02d/%02d", now.getMonthValue(), now.getYear() % 100);
        YearMonth past = now.minusMonths(1);
        String pastMonthStr = String.format("%02d/%02d", past.getMonthValue(), past.getYear() % 100);
        YearMonth future = now.plusMonths(1);
        String futureMonthStr = String.format("%02d/%02d", future.getMonthValue(), future.getYear() % 100);

        check("Current month (" + currentMonthStr + ") accepted (valid until end of month)",
                FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", currentMonthStr, "123").isValid(), "current month failed");
        check("Past month (" + pastMonthStr + ") rejected (expired)",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", pastMonthStr, "123").isValid(), "past month accepted");
        check("Future month (" + futureMonthStr + ") accepted",
                FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", futureMonthStr, "123").isValid(), "future month failed");

        check("Month 00 rejected",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", "00/28", "123").isValid(), "month 00 accepted");
        check("Month 13 rejected",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", "13/28", "123").isValid(), "month 13 accepted");
        check("Bad expiry format '12-28' rejected (requires slash)",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", "12-28", "123").isValid(), "hyphen expiry accepted");
        check("Bad expiry format '1/28' rejected (requires 2 digits month)",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", "1/28", "123").isValid(), "1 digit month accepted");
        check("Bad expiry format '12/2028' rejected (requires 2 digits year)",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", "12/2028", "123").isValid(), "4 digit year accepted");

        // CVV validation
        check("Null CVV rejected",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", "12/28", null).isValid(), "null cvv accepted");
        check("2-digit CVV '12' rejected (min 3)",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", "12/28", "12").isValid(), "2-digit cvv accepted");
        check("3-digit CVV '123' accepted",
                FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", "12/28", "123").isValid(), "3-digit cvv failed");
        check("4-digit CVV '1234' accepted (Amex format)",
                FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", "12/28", "1234").isValid(), "4-digit cvv failed");
        check("5-digit CVV '12345' rejected (max 4)",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", "12/28", "12345").isValid(), "5-digit cvv accepted");
        check("Alphabetic CVV '12A' rejected",
                !FrontendInputValidator.validateStripeCredentials("John Doe", "4242424242424242", "12/28", "12A").isValid(), "alphabetic cvv accepted");
    }

    // =========================================================================
    // 5. PAYPAL PAYMENT CREDENTIALS STRESS TESTS
    // =========================================================================
    private static void runPayPalCredentialsSuite() {
        System.out.println("\n[SUITE 5] PayPal Payment Credentials");

        // Email address boundaries
        check("Null email rejected", !FrontendInputValidator.validatePayPalCredentials(null, "8821").isValid(), "null email accepted");
        check("Empty email rejected", !FrontendInputValidator.validatePayPalCredentials("", "8821").isValid(), "empty email accepted");
        check("Whitespace email rejected", !FrontendInputValidator.validatePayPalCredentials("   ", "8821").isValid(), "whitespace email accepted");
        check("Email missing @ rejected", !FrontendInputValidator.validatePayPalCredentials("notanemail.com", "8821").isValid(), "no @ accepted");
        check("Email missing user part '@domain.com' rejected", !FrontendInputValidator.validatePayPalCredentials("@domain.com", "8821").isValid(), "no user accepted");
        check("Email missing domain 'user@' rejected", !FrontendInputValidator.validatePayPalCredentials("user@", "8821").isValid(), "no domain accepted");
        check("Email missing TLD dot 'user@domain' rejected", !FrontendInputValidator.validatePayPalCredentials("user@domain", "8821").isValid(), "no TLD dot accepted");
        check("Email with 1-char TLD 'user@domain.c' rejected", !FrontendInputValidator.validatePayPalCredentials("user@domain.c", "8821").isValid(), "1-char TLD accepted");
        check("Email with spaces 'user name@domain.com' rejected", !FrontendInputValidator.validatePayPalCredentials("user name@domain.com", "8821").isValid(), "space email accepted");

        // Valid emails
        check("Standard email 'user@example.com' accepted",
                FrontendInputValidator.validatePayPalCredentials("user@example.com", "8821").isValid(), "standard email failed");
        check("Subdomain & plus-tag email 'first.last+tag@sub.domain.co.uk' accepted",
                FrontendInputValidator.validatePayPalCredentials("first.last+tag@sub.domain.co.uk", "8821").isValid(), "plus tag email failed");

        // PIN / Token boundaries
        check("Null PIN rejected", !FrontendInputValidator.validatePayPalCredentials("user@example.com", null).isValid(), "null PIN accepted");
        check("Empty PIN rejected", !FrontendInputValidator.validatePayPalCredentials("user@example.com", "").isValid(), "empty PIN accepted");
        check("3-char PIN '123' rejected (min 4)", !FrontendInputValidator.validatePayPalCredentials("user@example.com", "123").isValid(), "3-char PIN accepted");
        check("4-char PIN '1234' accepted", FrontendInputValidator.validatePayPalCredentials("user@example.com", "1234").isValid(), "4-char PIN failed");
        check("Long PIN/token 'abc-12345-token' accepted", FrontendInputValidator.validatePayPalCredentials("user@example.com", "abc-12345-token").isValid(), "long PIN failed");
    }

    // =========================================================================
    // 6. CRYPTO WEB3 CREDENTIALS STRESS TESTS
    // =========================================================================
    private static void runCryptoCredentialsSuite() {
        System.out.println("\n[SUITE 6] Crypto Web3 Payment Credentials");

        String validAddr = "0x71C8FB9bC2525D4f28E8C3411bA67C429188aB29"; // 42 chars (0x + 40 hex)
        String validNetwork = "USDT ERC-20 (Ethereum)";

        // Address null / empty / whitespace
        check("Null crypto address rejected", !FrontendInputValidator.validateCryptoCredentials(null, validNetwork).isValid(), "null addr accepted");
        check("Empty crypto address rejected", !FrontendInputValidator.validateCryptoCredentials("", validNetwork).isValid(), "empty addr accepted");
        check("Whitespace crypto address rejected", !FrontendInputValidator.validateCryptoCredentials("   ", validNetwork).isValid(), "whitespace addr accepted");

        // Valid address
        check("Valid 42-char hex address accepted", FrontendInputValidator.validateCryptoCredentials(validAddr, validNetwork).isValid(), "valid addr failed");
        check("Valid all-lowercase hex address accepted", FrontendInputValidator.validateCryptoCredentials(validAddr.toLowerCase(), validNetwork).isValid(), "lowercase addr failed");
        check("Valid all-uppercase hex address (except 0x) accepted",
                FrontendInputValidator.validateCryptoCredentials("0x" + validAddr.substring(2).toUpperCase(), validNetwork).isValid(), "uppercase addr failed");

        // Address length anomalies
        String short39Hex = validAddr.substring(0, 41); // 41 chars (0x + 39 hex)
        check("Short address (41 chars: 0x + 39 hex) rejected", !FrontendInputValidator.validateCryptoCredentials(short39Hex, validNetwork).isValid(), "short 41 chars accepted");

        String long41Hex = validAddr + "a"; // 43 chars (0x + 41 hex)
        check("Long address (43 chars: 0x + 41 hex) rejected", !FrontendInputValidator.validateCryptoCredentials(long41Hex, validNetwork).isValid(), "long 43 chars accepted");

        // Missing 0x prefix
        String noPrefix = validAddr.substring(2); // 40 chars without 0x
        check("Address missing '0x' prefix rejected", !FrontendInputValidator.validateCryptoCredentials(noPrefix, validNetwork).isValid(), "no 0x prefix accepted");

        // Non-hex characters
        String nonHexAddr = "0x71C8FB9bC2525D4f28E8C3411bA67C429188aB2G"; // Ends with 'G'
        check("Address with non-hex character 'G' rejected", !FrontendInputValidator.validateCryptoCredentials(nonHexAddr, validNetwork).isValid(), "non-hex char accepted");

        // Network validation
        check("Null settlement network rejected", !FrontendInputValidator.validateCryptoCredentials(validAddr, null).isValid(), "null network accepted");
        check("Empty settlement network rejected", !FrontendInputValidator.validateCryptoCredentials(validAddr, "").isValid(), "empty network accepted");
        check("Whitespace settlement network rejected", !FrontendInputValidator.validateCryptoCredentials(validAddr, "   ").isValid(), "whitespace network accepted");
        check("Valid TRC-20 network accepted", FrontendInputValidator.validateCryptoCredentials(validAddr, "USDT TRC-20 (Tron)").isValid(), "TRC-20 network failed");
    }

    // =========================================================================
    // 7. LUHN CHECKSUM EXHAUSTIVE VERIFICATION
    // =========================================================================
    private static void runLuhnChecksumExhaustiveSuite() {
        System.out.println("\n[SUITE 7] Luhn Checksum Algorithm Stress & Oracles");

        // Known Luhn valid numbers
        check("Standard 4242424242424242 passes Luhn", FrontendInputValidator.passesLuhnCheck("4242424242424242"), "4242 failed");
        check("Mastercard 5555555555554444 passes Luhn", FrontendInputValidator.passesLuhnCheck("5555555555554444"), "5555 failed");
        check("Wikipedia Luhn example '49927398716' passes Luhn", FrontendInputValidator.passesLuhnCheck("49927398716"), "Wikipedia Luhn failed");
        check("Single-digit '0' passes Luhn (0 % 10 == 0)", FrontendInputValidator.passesLuhnCheck("0"), "0 failed");

        // Known Luhn invalid numbers
        check("4242424242424241 fails Luhn (bad check digit)", !FrontendInputValidator.passesLuhnCheck("4242424242424241"), "4242424242424241 passed Luhn");
        check("4000000000000001 fails Luhn", !FrontendInputValidator.passesLuhnCheck("4000000000000001"), "4000000000000001 passed Luhn");
        check("1111111111111111 fails Luhn", !FrontendInputValidator.passesLuhnCheck("1111111111111111"), "1111111111111111 passed Luhn");

        // Edge case inputs to passesLuhnCheck
        check("Null digits string fails Luhn", !FrontendInputValidator.passesLuhnCheck(null), "null passed");
        check("Empty digits string fails Luhn", !FrontendInputValidator.passesLuhnCheck(""), "empty passed");
        check("Alphabetic string 'abcdef' fails Luhn", !FrontendInputValidator.passesLuhnCheck("abcdef"), "alpha passed");
        check("String with embedded non-digit '424242424242424A' fails Luhn", !FrontendInputValidator.passesLuhnCheck("424242424242424A"), "non-digit passed");

        // Oracle Test: Generate 50 valid Luhn numbers and 50 corrupted numbers
        int oracleSuccessCount = 0;
        for (int seed = 1000; seed < 1050; seed++) {
            String prefix = "4000" + seed + "000"; // 11 digits
            int checkDigit = computeExpectedLuhnCheckDigit(prefix);
            String fullCard = prefix + checkDigit;
            if (FrontendInputValidator.passesLuhnCheck(fullCard)) {
                oracleSuccessCount++;
            }
            // Corrupt check digit
            int badCheckDigit = (checkDigit + 1) % 10;
            String badCard = prefix + badCheckDigit;
            if (!FrontendInputValidator.passesLuhnCheck(badCard)) {
                oracleSuccessCount++;
            }
        }
        check("Oracle verification of 50 generated valid and 50 corrupted cards (100 tests)",
                oracleSuccessCount == 100, "Oracle mismatch count: " + (100 - oracleSuccessCount));
    }

    // =========================================================================
    // 8. SWING UI EVENT HANDLER INTEGRATION TESTS
    // =========================================================================
    private static void runUiEventIntegrationSuite() {
        System.out.println("\n[SUITE 8] Swing UI Event Handler Integration Stress Tests");

        FrontendLogicTest.MockUiFeedbackService mockUi = new FrontendLogicTest.MockUiFeedbackService();
        EcommerceSwingApp app = new EcommerceSwingApp(mockUi);

        // 1. Catalog without selection
        app.getCatalogTable().clearSelection();
        mockUi.reset();
        app.handleAddSelectedCatalogItemToCart();
        check("UI: Add to cart without product selected triggers warning",
                mockUi.warningCount == 1 && mockUi.lastTitle.contains("No Product Selected"),
                "warnings: " + mockUi.warningCount + ", title: " + mockUi.lastTitle);

        // 2. Select product (row 0: Galaxy Ultra Pro, stock = 8)
        app.getCatalogTable().setRowSelectionInterval(0, 0);
        app.getCatalogQtySpinner().setValue(10); // Exceeds stock of 8
        mockUi.reset();
        app.handleAddSelectedCatalogItemToCart();
        check("UI: Adding quantity > stock triggers warning and rejects addition",
                mockUi.warningCount == 1 && app.getShoppingCart().isEmpty(),
                "warnings: " + mockUi.warningCount + ", cartItems: " + app.getShoppingCart().getItemCount());

        // 3. Add valid quantity (5 out of 8)
        app.getCatalogQtySpinner().setValue(5);
        mockUi.reset();
        app.handleAddSelectedCatalogItemToCart();
        check("UI: Adding valid quantity within stock succeeds",
                mockUi.infoCount == 1 && app.getShoppingCart().getItemCount() == 5,
                "infoCount: " + mockUi.infoCount + ", cartCount: " + app.getShoppingCart().getItemCount());

        // 4. Cumulative addition overflow (currently 5 in cart, requested 4, stock 8 -> 5+4=9 > 8)
        app.getCatalogQtySpinner().setValue(4);
        mockUi.reset();
        app.handleAddSelectedCatalogItemToCart();
        check("UI: Cumulative cart allocation overflow (5 in cart + 4 requested > 8 stock) rejected with warning",
                mockUi.warningCount == 1 && app.getShoppingCart().getItemCount() == 5,
                "warnings: " + mockUi.warningCount + ", cartCount: " + app.getShoppingCart().getItemCount());

        // 5. Cumulative exact addition (currently 5, requested 3 -> 5+3=8 <= 8)
        app.getCatalogQtySpinner().setValue(3);
        mockUi.reset();
        app.handleAddSelectedCatalogItemToCart();
        check("UI: Cumulative exact allocation (5 in cart + 3 requested == 8 stock) accepted",
                mockUi.infoCount == 1 && app.getShoppingCart().getItemCount() == 8,
                "infoCount: " + mockUi.infoCount + ", cartCount: " + app.getShoppingCart().getItemCount());

        // 6. Cumulative full: 8 in cart out of 8 stock. Requesting 1 more must fail
        app.getCatalogQtySpinner().setValue(1);
        mockUi.reset();
        app.handleAddSelectedCatalogItemToCart();
        check("UI: Additional unit when at max stock limit rejected",
                mockUi.warningCount == 1 && app.getShoppingCart().getItemCount() == 8,
                "warnings: " + mockUi.warningCount + ", cartCount: " + app.getShoppingCart().getItemCount());

        // 7. Custom PC Builder UI: Blank name
        app.getCustomBuildNameField().setText("   ");
        mockUi.reset();
        app.handleAssembleCustomRig();
        check("UI: Blank Custom Rig Name triggers warning",
                mockUi.warningCount == 1 && mockUi.lastTitle.contains("Custom Rig Configuration Error"),
                "warnings: " + mockUi.warningCount + ", title: " + mockUi.lastTitle);

        // 8. Custom PC Builder UI: > 50 chars
        app.getCustomBuildNameField().setText("A".repeat(55));
        mockUi.reset();
        app.handleAssembleCustomRig();
        check("UI: 55-char Custom Rig Name triggers warning",
                mockUi.warningCount == 1 && mockUi.lastMessage.contains("cannot exceed 50 characters"),
                "warnings: " + mockUi.warningCount + ", msg: " + mockUi.lastMessage);

        // 9. Promo code UI: Empty promo code
        app.getPromoCodeField().setText("");
        mockUi.reset();
        app.handleApplyPromoCode();
        check("UI: Empty promo code triggers warning",
                mockUi.warningCount == 1 && mockUi.lastTitle.contains("Promo Code Required"),
                "warnings: " + mockUi.warningCount + ", title: " + mockUi.lastTitle);

        // 10. Promo code UI: Invalid code
        app.getPromoCodeField().setText("INVALID_CODE_99");
        mockUi.reset();
        app.handleApplyPromoCode();
        check("UI: Invalid promo code triggers qualification warning",
                mockUi.warningCount == 1 && mockUi.lastTitle.contains("Promo Code Qualification Error"),
                "warnings: " + mockUi.warningCount + ", title: " + mockUi.lastTitle);

        // 11. Empty cart checkout
        EcommerceSwingApp emptyApp = new EcommerceSwingApp(mockUi);
        mockUi.reset();
        emptyApp.handleExecuteCheckout();
        check("UI: Checkout on empty shopping cart triggers warning and halts",
                mockUi.warningCount == 1 && mockUi.lastTitle.contains("Empty Cart"),
                "warnings: " + mockUi.warningCount + ", title: " + mockUi.lastTitle);
    }

    private static int computeExpectedLuhnCheckDigit(String numberWithoutCheck) {
        int sum = 0;
        boolean alternate = true;
        for (int i = numberWithoutCheck.length() - 1; i >= 0; i--) {
            int n = numberWithoutCheck.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }
}
