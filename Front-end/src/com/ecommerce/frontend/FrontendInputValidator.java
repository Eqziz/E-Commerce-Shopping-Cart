package com.ecommerce.frontend;

import java.time.YearMonth;
import java.util.regex.Pattern;

/**
 * Pure frontend input validation engine for user interactions in the GUI.
 * Validates catalog quantities, custom PC builder configurations,
 * promotional discount codes, and payment gateway credentials.
 */
public final class FrontendInputValidator {

    private static final Pattern PAYPAL_EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern CRYPTO_ADDRESS_PATTERN =
            Pattern.compile("^0x[0-9a-fA-F]{40}$");

    private static final Pattern CARDHOLDER_NAME_PATTERN =
            Pattern.compile("^[A-Za-z\\s'\\-\\.]+$");

    private static final Pattern CARD_EXPIRY_PATTERN =
            Pattern.compile("^(0[1-9]|1[0-2])\\/([0-9]{2})$");

    /**
     * Immutable outcome of an input validation operation.
     */
    public static final class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage != null ? errorMessage : "";
        }

        public static ValidationResult success() {
            return new ValidationResult(true, "");
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage != null ? errorMessage : "Validation error");
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            return valid ? "ValidationResult[VALID]" : "ValidationResult[INVALID: " + errorMessage + "]";
        }
    }

    private FrontendInputValidator() {
        // Utility class
    }

    /**
     * Validates requested item quantity against available stock and current cart allocations.
     *
     * @param requestedQty   Quantity user wants to add
     * @param currentCartQty Quantity already present in user's shopping cart
     * @param availableStock Total available stock in warehouse inventory
     * @return ValidationResult indicating success or failure with descriptive message
     */
    public static ValidationResult validateCatalogQuantity(int requestedQty, int currentCartQty, int availableStock) {
        if (requestedQty <= 0) {
            return ValidationResult.failure("Requested quantity must be a positive integer greater than zero.");
        }
        if (availableStock <= 0) {
            return ValidationResult.failure("This item is currently out of stock.");
        }
        if (requestedQty > availableStock) {
            return ValidationResult.failure("Requested quantity (" + requestedQty +
                    ") exceeds available warehouse stock (" + availableStock + ").");
        }
        int cartAlloc = Math.max(0, currentCartQty);
        if ((long) cartAlloc + (long) requestedQty > availableStock) {
            return ValidationResult.failure("Cannot add " + requestedQty + " unit(s). You already have " +
                    cartAlloc + " in your cart, and available stock is only " + availableStock + ".");
        }
        return ValidationResult.success();
    }

    /**
     * Validates a custom PC builder rig configuration before assembly and cart dispatch.
     *
     * @param rigName User-defined name for custom rig
     * @param cpu     Selected CPU component
     * @param ram     Selected RAM component
     * @param gpu     Selected GPU component
     * @param ssd     Selected SSD component
     * @param os      Selected OS component
     * @return ValidationResult indicating success or failure with descriptive message
     */
    public static ValidationResult validateCustomRig(String rigName, Object cpu, Object ram,
                                                    Object gpu, Object ssd, Object os) {
        if (rigName == null || rigName.trim().isEmpty()) {
            return ValidationResult.failure("Custom Rig Name cannot be empty. Please enter a valid name.");
        }
        String trimmed = rigName.trim();
        if (trimmed.length() > 50) {
            return ValidationResult.failure("Custom Rig Name cannot exceed 50 characters (current length: " +
                    trimmed.length() + ").");
        }
        if (cpu == null || ram == null || gpu == null || ssd == null || os == null) {
            return ValidationResult.failure("All hardware component options (CPU, RAM, GPU, SSD, OS) must be selected.");
        }
        return ValidationResult.success();
    }

    /**
     * Validates promotional discount code against recognized codes and spend thresholds.
     *
     * @param promoCode User-entered promo code string
     * @param subtotal  Current order subtotal
     * @return ValidationResult indicating success or failure with descriptive message
     */
    public static ValidationResult validatePromoCode(String promoCode, double subtotal) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return ValidationResult.failure("Promotional code cannot be empty.");
        }
        String code = promoCode.trim().toUpperCase();
        switch (code) {
            case "SAVE10":
            case "FALL15":
            case "BOGO":
            case "LOYALTY":
            case "NONE":
                return ValidationResult.success();
            case "SAVE50":
                if (subtotal < 250.00) {
                    return ValidationResult.failure(String.format(
                            "Promo code SAVE50 requires a minimum order spend of $250.00. Current order subtotal is $%.2f.",
                            subtotal));
                }
                return ValidationResult.success();
            default:
                return ValidationResult.failure("Unrecognized or expired promotional code: '" +
                        promoCode.trim() + "'. Please check the code and try again.");
        }
    }

    /**
     * Validates Stripe credit / debit card payment credentials.
     *
     * @param cardholder Full name of the cardholder
     * @param cardNumber 16-digit card number (spaces and hyphens permitted)
     * @param expiry     Expiration date in MM/YY format
     * @param cvv        3 or 4 digit card verification value
     * @return ValidationResult
     */
    public static ValidationResult validateStripeCredentials(String cardholder, String cardNumber,
                                                            String expiry, String cvv) {
        if (cardholder == null || cardholder.trim().isEmpty()) {
            return ValidationResult.failure("Cardholder full name is required.");
        }
        String trimmedName = cardholder.trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 70) {
            return ValidationResult.failure("Cardholder name must be between 2 and 70 characters.");
        }
        if (!CARDHOLDER_NAME_PATTERN.matcher(trimmedName).matches()) {
            return ValidationResult.failure("Cardholder name contains invalid characters. Use letters, spaces, hyphens, and apostrophes only.");
        }

        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return ValidationResult.failure("Credit / Debit card number is required.");
        }
        String digits = cardNumber.replaceAll("[\\s\\-]", "");
        if (!digits.matches("^\\d{16}$")) {
            return ValidationResult.failure("Card number must be exactly 16 numeric digits.");
        }
        if (!passesLuhnCheck(digits)) {
            return ValidationResult.failure("Card number failed the Luhn checksum verification algorithm.");
        }

        if (expiry == null || expiry.trim().isEmpty()) {
            return ValidationResult.failure("Card expiration date is required (MM/YY format).");
        }
        String exp = expiry.trim();
        if (!CARD_EXPIRY_PATTERN.matcher(exp).matches()) {
            return ValidationResult.failure("Card expiration date must be in valid MM/YY format (e.g. 12/28).");
        }
        int expMonth = Integer.parseInt(exp.substring(0, 2));
        int expYear = 2000 + Integer.parseInt(exp.substring(3, 5));
        YearMonth currentYm = YearMonth.now();
        YearMonth cardYm = YearMonth.of(expYear, expMonth);
        if (cardYm.isBefore(currentYm)) {
            return ValidationResult.failure("Card has expired (expiration date: " + exp + ").");
        }

        if (cvv == null || cvv.trim().isEmpty()) {
            return ValidationResult.failure("Card CVV / CVC code is required.");
        }
        String trimmedCvv = cvv.trim();
        if (!trimmedCvv.matches("^\\d{3,4}$")) {
            return ValidationResult.failure("Card CVV / CVC must be 3 or 4 numeric digits.");
        }

        return ValidationResult.success();
    }

    /**
     * Validates PayPal payment credentials.
     *
     * @param email PayPal account email address
     * @param pin   Security PIN or authentication token
     * @return ValidationResult
     */
    public static ValidationResult validatePayPalCredentials(String email, String pin) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.failure("PayPal email address is required.");
        }
        String trimmedEmail = email.trim();
        if (!PAYPAL_EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            return ValidationResult.failure("Invalid PayPal email format ('" + trimmedEmail + "'). Please enter a valid email address.");
        }
        if (pin == null || pin.trim().isEmpty()) {
            return ValidationResult.failure("PayPal security PIN or access token is required.");
        }
        if (pin.trim().length() < 4) {
            return ValidationResult.failure("PayPal security PIN or access token must be at least 4 characters.");
        }
        return ValidationResult.success();
    }

    /**
     * Validates Web3 / Cryptocurrency payment credentials.
     *
     * @param walletAddress Hexadecimal wallet address starting with 0x (42 characters total)
     * @param network       Cryptocurrency blockchain network name
     * @return ValidationResult
     */
    public static ValidationResult validateCryptoCredentials(String walletAddress, String network) {
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            return ValidationResult.failure("Crypto wallet address is required.");
        }
        String trimmedAddr = walletAddress.trim();
        if (!CRYPTO_ADDRESS_PATTERN.matcher(trimmedAddr).matches()) {
            return ValidationResult.failure("Invalid Web3 wallet address. Must be a 42-character hexadecimal string starting with '0x'.");
        }
        if (network == null || network.trim().isEmpty()) {
            return ValidationResult.failure("Cryptocurrency settlement network selection is required.");
        }
        return ValidationResult.success();
    }

    /**
     * Verifies if a numeric digit string satisfies the Luhn mod-10 algorithm.
     *
     * @param digits String containing exclusively numeric digits
     * @return true if valid Luhn checksum, false otherwise
     */
    public static boolean passesLuhnCheck(String digits) {
        if (digits == null || digits.isEmpty()) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            char c = digits.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            int n = c - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}
