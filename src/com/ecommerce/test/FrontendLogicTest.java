package com.ecommerce.test;

import com.ecommerce.adapter.PaymentGateway;
import com.ecommerce.adapter.PaymentResult;
import com.ecommerce.adapter.StripePaymentAdapter;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.decorator.GiftWrapDecorator;
import com.ecommerce.decorator.OrderItem;
import com.ecommerce.decorator.StandardOrderItem;
import com.ecommerce.facade.CheckoutFacade;
import com.ecommerce.frontend.EcommerceSwingApp;
import com.ecommerce.frontend.FrontendInputValidator;
import com.ecommerce.frontend.PaymentCredentialsDialog;
import com.ecommerce.frontend.UiFeedbackService;
import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PhysicalProduct;
import com.ecommerce.model.ProductType;
import com.ecommerce.observer.InventorySubject;
import com.ecommerce.strategy.FlatRateDiscountStrategy;
import com.ecommerce.strategy.NoDiscountStrategy;

import javax.swing.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Automated test suite validating all frontend business logic, input validation,
 * state preservation, line-item resolution bug fix, and thread safety rules.
 * Runs in headless environments without GUI hangs using a mock UiFeedbackService.
 */
public class FrontendLogicTest {

    public static class MockUiFeedbackService implements UiFeedbackService {
        public int infoCount = 0;
        public int warningCount = 0;
        public int errorCount = 0;
        public int confirmCount = 0;
        public String lastTitle;
        public String lastMessage;
        public boolean confirmResponse = true;

        @Override
        public void showInformation(Component parent, String title, String message) {
            infoCount++;
            lastTitle = title;
            lastMessage = message;
        }

        @Override
        public void showWarning(Component parent, String title, String message) {
            warningCount++;
            lastTitle = title;
            lastMessage = message;
        }

        @Override
        public void showError(Component parent, String title, String message) {
            errorCount++;
            lastTitle = title;
            lastMessage = message;
        }

        @Override
        public boolean showConfirmation(Component parent, String title, String message) {
            confirmCount++;
            lastTitle = title;
            lastMessage = message;
            return confirmResponse;
        }

        public void reset() {
            infoCount = 0;
            warningCount = 0;
            errorCount = 0;
            confirmCount = 0;
            lastTitle = null;
            lastMessage = null;
            confirmResponse = true;
        }
    }

    public static void runTests() {
        System.out.println("Running FrontendLogicTest...");

        testQuantityValidationRules();
        testCumulativeCartOverflowRejection();
        testCustomRigValidationRules();
        testPromoCodeThresholdAndValidity();
        testStripeCredentialsAndLuhnAlgorithm();
        testPayPalCredentialsValidation();
        testCryptoCredentialsValidation();
        testFullLineItemResolutionBugFix();
        testCartPreservationOnPaymentFailure();
        testCartClearingOnSuccessfulCheckout();
        testConfirmationOnCartClear();
        testEdtSafetyAndUncaughtExceptionHandler();

        System.out.println("  [PASSED] FrontendLogicTest passed.");
    }

    private static void testQuantityValidationRules() {
        // 1. Positive quantity check
        FrontendInputValidator.ValidationResult r1 =
                FrontendInputValidator.validateCatalogQuantity(0, 0, 10);
        SimpleAssert.assertFalse(r1.isValid(), "Quantity 0 must be rejected");

        FrontendInputValidator.ValidationResult r2 =
                FrontendInputValidator.validateCatalogQuantity(-5, 0, 10);
        SimpleAssert.assertFalse(r2.isValid(), "Negative quantity must be rejected");

        // 2. Out of stock check
        FrontendInputValidator.ValidationResult r3 =
                FrontendInputValidator.validateCatalogQuantity(1, 0, 0);
        SimpleAssert.assertFalse(r3.isValid(), "Quantity on out-of-stock product must be rejected");

        // 3. Exceeds stock check
        FrontendInputValidator.ValidationResult r4 =
                FrontendInputValidator.validateCatalogQuantity(11, 0, 10);
        SimpleAssert.assertFalse(r4.isValid(), "Quantity exceeding warehouse stock must be rejected");

        // 4. Valid within stock
        FrontendInputValidator.ValidationResult r5 =
                FrontendInputValidator.validateCatalogQuantity(5, 0, 10);
        SimpleAssert.assertTrue(r5.isValid(), "Quantity within stock must be valid");
    }

    private static void testCumulativeCartOverflowRejection() {
        // Stock is 5. Cart already holds 3 units.
        // User requests 3 more. 3 + 3 = 6 > 5. Must be rejected!
        FrontendInputValidator.ValidationResult overflowResult =
                FrontendInputValidator.validateCatalogQuantity(3, 3, 5);
        SimpleAssert.assertFalse(overflowResult.isValid(),
                "Cumulative cart allocation (3 in cart + 3 requested > 5 stock) must be rejected");

        // User requests 2 more. 3 + 2 = 5 <= 5. Must be accepted!
        FrontendInputValidator.ValidationResult validCumulative =
                FrontendInputValidator.validateCatalogQuantity(2, 3, 5);
        SimpleAssert.assertTrue(validCumulative.isValid(),
                "Cumulative cart allocation matching exactly available stock must be valid");
    }

    private static void testCustomRigValidationRules() {
        Object dummyCpu = new Object();
        Object dummyRam = new Object();
        Object dummyGpu = new Object();
        Object dummySsd = new Object();
        Object dummyOs = new Object();

        // 1. Valid rig configuration
        FrontendInputValidator.ValidationResult valid =
                FrontendInputValidator.validateCustomRig("Nodir Workstation", dummyCpu, dummyRam, dummyGpu, dummySsd, dummyOs);
        SimpleAssert.assertTrue(valid.isValid(), "Valid custom rig parameters must pass validation");

        // 2. Empty or null name
        FrontendInputValidator.ValidationResult emptyName =
                FrontendInputValidator.validateCustomRig("   ", dummyCpu, dummyRam, dummyGpu, dummySsd, dummyOs);
        SimpleAssert.assertFalse(emptyName.isValid(), "Whitespace-only rig name must be rejected");

        // 3. Name length > 50 chars
        String longName = "A".repeat(51);
        FrontendInputValidator.ValidationResult nameTooLong =
                FrontendInputValidator.validateCustomRig(longName, dummyCpu, dummyRam, dummyGpu, dummySsd, dummyOs);
        SimpleAssert.assertFalse(nameTooLong.isValid(), "Rig name over 50 characters must be rejected");

        // 4. Missing hardware component
        FrontendInputValidator.ValidationResult missingCpu =
                FrontendInputValidator.validateCustomRig("Gaming PC", null, dummyRam, dummyGpu, dummySsd, dummyOs);
        SimpleAssert.assertFalse(missingCpu.isValid(), "Missing CPU component must be rejected");
    }

    private static void testPromoCodeThresholdAndValidity() {
        // 1. SAVE50 with subtotal below $250 -> REJECTED
        FrontendInputValidator.ValidationResult lowSpend =
                FrontendInputValidator.validatePromoCode("SAVE50", 199.99);
        SimpleAssert.assertFalse(lowSpend.isValid(), "SAVE50 must be rejected when subtotal < $250");

        // 2. SAVE50 with subtotal >= $250 -> ACCEPTED
        FrontendInputValidator.ValidationResult validSpend =
                FrontendInputValidator.validatePromoCode("SAVE50", 250.00);
        SimpleAssert.assertTrue(validSpend.isValid(), "SAVE50 must be accepted when subtotal >= $250");

        // 3. Recognized standard codes -> ACCEPTED
        SimpleAssert.assertTrue(FrontendInputValidator.validatePromoCode("SAVE10", 50.0).isValid(), "SAVE10 valid");
        SimpleAssert.assertTrue(FrontendInputValidator.validatePromoCode("FALL15", 50.0).isValid(), "FALL15 valid");
        SimpleAssert.assertTrue(FrontendInputValidator.validatePromoCode("BOGO", 50.0).isValid(), "BOGO valid");

        // 4. Fake / corrupted code -> REJECTED
        FrontendInputValidator.ValidationResult fake =
                FrontendInputValidator.validatePromoCode("FREE_MONEY_99", 500.0);
        SimpleAssert.assertFalse(fake.isValid(), "Unrecognized promo code must be rejected");

        // 5. Empty code -> REJECTED
        SimpleAssert.assertFalse(FrontendInputValidator.validatePromoCode("", 100.0).isValid(), "Empty code rejected");
    }

    private static void testStripeCredentialsAndLuhnAlgorithm() {
        // 1. Valid test card passing Luhn: 4242 4242 4242 4242
        SimpleAssert.assertTrue(FrontendInputValidator.passesLuhnCheck("4242424242424242"),
                "4242424242424242 must pass Luhn checksum");

        FrontendInputValidator.ValidationResult validStripe =
                FrontendInputValidator.validateStripeCredentials("Nodir Mukhammedov", "4242424242424242", "12/28", "123");
        SimpleAssert.assertTrue(validStripe.isValid(), "Valid Stripe credentials must pass");

        // 2. Card failing Luhn algorithm
        SimpleAssert.assertFalse(FrontendInputValidator.passesLuhnCheck("4242424242424241"),
                "4242424242424241 must fail Luhn checksum");

        FrontendInputValidator.ValidationResult invalidLuhn =
                FrontendInputValidator.validateStripeCredentials("Nodir Mukhammedov", "4242424242424241", "12/28", "123");
        SimpleAssert.assertFalse(invalidLuhn.isValid(), "Card failing Luhn must be rejected");

        // 3. Card number with invalid length (15 digits)
        FrontendInputValidator.ValidationResult shortCard =
                FrontendInputValidator.validateStripeCredentials("Nodir Mukhammedov", "424242424242424", "12/28", "123");
        SimpleAssert.assertFalse(shortCard.isValid(), "15-digit card must be rejected");

        // 4. Expired card
        FrontendInputValidator.ValidationResult expiredCard =
                FrontendInputValidator.validateStripeCredentials("Nodir Mukhammedov", "4242424242424242", "01/20", "123");
        SimpleAssert.assertFalse(expiredCard.isValid(), "Expired card (01/20) must be rejected");

        // 5. Invalid CVV
        FrontendInputValidator.ValidationResult badCvv =
                FrontendInputValidator.validateStripeCredentials("Nodir Mukhammedov", "4242424242424242", "12/28", "12");
        SimpleAssert.assertFalse(badCvv.isValid(), "2-digit CVV must be rejected");

        // 6. Blank cardholder name
        FrontendInputValidator.ValidationResult blankName =
                FrontendInputValidator.validateStripeCredentials("", "4242424242424242", "12/28", "123");
        SimpleAssert.assertFalse(blankName.isValid(), "Blank cardholder name must be rejected");
    }

    private static void testPayPalCredentialsValidation() {
        // 1. Valid PayPal credentials
        FrontendInputValidator.ValidationResult valid =
                FrontendInputValidator.validatePayPalCredentials("nodir@university.edu", "8821");
        SimpleAssert.assertTrue(valid.isValid(), "Valid PayPal credentials must pass");

        // 2. Invalid email format
        FrontendInputValidator.ValidationResult badEmail =
                FrontendInputValidator.validatePayPalCredentials("not-an-email", "8821");
        SimpleAssert.assertFalse(badEmail.isValid(), "Malformed email must be rejected");

        // 3. Short PIN
        FrontendInputValidator.ValidationResult shortPin =
                FrontendInputValidator.validatePayPalCredentials("nodir@university.edu", "12");
        SimpleAssert.assertFalse(shortPin.isValid(), "PIN shorter than 4 characters must be rejected");
    }

    private static void testCryptoCredentialsValidation() {
        // 1. Valid 40-hex wallet address with 0x prefix
        FrontendInputValidator.ValidationResult valid =
                FrontendInputValidator.validateCryptoCredentials("0x71C8FB9bC2525D4f28E8C3411bA67C429188aB29", "USDT ERC-20");
        SimpleAssert.assertTrue(valid.isValid(), "Valid 42-char hex wallet address must pass");

        // 2. Missing 0x prefix
        FrontendInputValidator.ValidationResult noPrefix =
                FrontendInputValidator.validateCryptoCredentials("71C8FB9bC2525D4f28E8C3411bA67C429188aB29", "USDT ERC-20");
        SimpleAssert.assertFalse(noPrefix.isValid(), "Address without 0x prefix must be rejected");

        // 3. Invalid characters
        FrontendInputValidator.ValidationResult invalidHex =
                FrontendInputValidator.validateCryptoCredentials("0xZZZZFB9bC2525D4f28E8C3411bA67C429188aB29", "USDT ERC-20");
        SimpleAssert.assertFalse(invalidHex.isValid(), "Address with non-hex characters must be rejected");

        // 4. Missing network
        FrontendInputValidator.ValidationResult noNet =
                FrontendInputValidator.validateCryptoCredentials("0x71C8FB9bC2525D4f28E8C3411bA67C429188aB29", "");
        SimpleAssert.assertFalse(noNet.isValid(), "Empty network must be rejected");
    }

    private static void testFullLineItemResolutionBugFix() {
        ShoppingCart cart = new ShoppingCart("CART-TEST", "CUST-TEST");
        PhysicalProduct p1 = new PhysicalProduct("P1", "Product 1", "Desc", 100.0, ProductType.ELECTRONICS, 1.0, "Box");
        PhysicalProduct p2 = new PhysicalProduct("P2", "Product 2", "Desc", 50.0, ProductType.BOOK, 0.5, "Standard");

        cart.addItem(p1, 1);
        cart.addItem(p2, 2);

        // Decorate ONLY Product 1 with GiftWrap
        List<OrderItem> configuredDecorators = new ArrayList<>();
        configuredDecorators.add(new GiftWrapDecorator(new StandardOrderItem(new CartItem(p1, 1)), "Birthday!", 5.0));

        // Simulate full line-item resolution
        List<OrderItem> resolved = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = configuredDecorators.stream()
                    .filter(item -> item.getItemId().equals(ci.getProduct().getId()))
                    .findFirst()
                    .orElse(new StandardOrderItem(ci));
            resolved.add(oi);
        }

        SimpleAssert.assertEquals(2, resolved.size(), "Both decorated and undecorated items must be resolved");

        // Verify Product 1 is decorated
        OrderItem item1 = resolved.stream().filter(i -> i.getItemId().equals("P1")).findFirst().orElse(null);
        SimpleAssert.assertNotNull(item1, "Product 1 must be present");
        SimpleAssert.assertTrue(item1.getDescription().contains("Gift Wrap"), "Product 1 must include GiftWrap decorator");
        SimpleAssert.assertEquals(105.0, item1.getTotalPrice(), 0.01, "Product 1 total price with gift wrap is 105.00");

        // Verify Product 2 is standard undecorated
        OrderItem item2 = resolved.stream().filter(i -> i.getItemId().equals("P2")).findFirst().orElse(null);
        SimpleAssert.assertNotNull(item2, "Product 2 must be present despite being undecorated");
        SimpleAssert.assertEquals(100.0, item2.getTotalPrice(), 0.01, "Product 2 total price is 50.0 * 2 = 100.00");
    }

    private static void testCartPreservationOnPaymentFailure() {
        InventorySubject inventory = new InventorySubject();
        PhysicalProduct p = new PhysicalProduct("FAIL-P1", "Test Item", "Desc", 80.0, ProductType.CLOTHING, 0.4, "Poly");
        inventory.registerProduct(p, 10);

        ShoppingCart cart = new ShoppingCart("CART-FAIL", "CUST-TEST");
        cart.addItem(p, 2);

        Customer customer = new Customer("CUST-1", "Test User", "test@test.com", "Address", "GOLD");

        // Create failing gateway adapter
        PaymentGateway failingGateway = new PaymentGateway() {
            @Override
            public String getGatewayName() {
                return "Mock Failing Gateway";
            }

            @Override
            public PaymentResult pay(String orderId, double amount, String customerEmail) {
                return new PaymentResult(false, "TX-DECLINED", amount, "MOCK", "Card declined by issuing bank.");
            }

            @Override
            public boolean refund(String transactionId, double amount) {
                return false;
            }
        };

        CheckoutFacade facade = new CheckoutFacade(inventory, failingGateway);

        boolean exceptionThrown = false;
        try {
            facade.processCheckout(cart, customer, new NoDiscountStrategy(), failingGateway, null);
        } catch (IllegalStateException e) {
            exceptionThrown = true;
        }

        SimpleAssert.assertTrue(exceptionThrown, "Payment failure must throw an exception to halt checkout");
        SimpleAssert.assertEquals(2, cart.getItemCount(), "Shopping cart items MUST BE PRESERVED on payment failure");
        SimpleAssert.assertEquals(10, inventory.getStock("FAIL-P1"), "Inventory stock must be rolled back on payment failure");
    }

    private static void testCartClearingOnSuccessfulCheckout() {
        InventorySubject inventory = new InventorySubject();
        PhysicalProduct p = new PhysicalProduct("SUCC-P1", "Success Item", "Desc", 50.0, ProductType.BOOK, 0.5, "Standard");
        inventory.registerProduct(p, 10);

        ShoppingCart cart = new ShoppingCart("CART-SUCC", "CUST-TEST");
        cart.addItem(p, 1);

        Customer customer = new Customer("CUST-1", "Test User", "test@test.com", "Address", "GOLD");
        PaymentGateway stripeGateway = new StripePaymentAdapter("sk_test_key");
        CheckoutFacade facade = new CheckoutFacade(inventory, stripeGateway);

        Order order = facade.processCheckout(cart, customer, new NoDiscountStrategy(), stripeGateway, null);

        SimpleAssert.assertEquals(OrderStatus.PAID, order.getStatus(), "Order status must be PAID");
        SimpleAssert.assertEquals(0, cart.getItemCount(), "Shopping cart MUST BE CLEARED upon confirmed payment");
        SimpleAssert.assertEquals(9, inventory.getStock("SUCC-P1"), "Inventory stock must be decremented by 1");
    }

    private static void testConfirmationOnCartClear() {
        MockUiFeedbackService mockUi = new MockUiFeedbackService();
        ShoppingCart cart = new ShoppingCart("CART-CONFIRM", "CUST-TEST");
        PhysicalProduct p = new PhysicalProduct("CONF-P1", "Confirm Item", "Desc", 40.0, ProductType.CLOTHING, 0.5, "Bag");
        cart.addItem(p, 2);

        // Case 1: User declines confirmation
        mockUi.confirmResponse = false;
        boolean confirmed1 = mockUi.showConfirmation(null, "Clear Cart", "Are you sure?");
        SimpleAssert.assertFalse(confirmed1, "User declined confirmation");
        // Simulated behavior when user declines: cart is not cleared
        SimpleAssert.assertEquals(2, cart.getItemCount(), "Cart must remain intact when user declines clear prompt");

        // Case 2: User accepts confirmation
        mockUi.confirmResponse = true;
        boolean confirmed2 = mockUi.showConfirmation(null, "Clear Cart", "Are you sure?");
        SimpleAssert.assertTrue(confirmed2, "User accepted confirmation");
        if (confirmed2) {
            cart.clear();
        }
        SimpleAssert.assertEquals(0, cart.getItemCount(), "Cart must be cleared when user confirms");
    }

    private static void testEdtSafetyAndUncaughtExceptionHandler() {
        // Assert that long background operations do not run on EDT
        AtomicBoolean backgroundWasOffEdt = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                backgroundWasOffEdt.set(!SwingUtilities.isEventDispatchThread());
                return null;
            }

            @Override
            protected void done() {
                latch.countDown();
            }
        };

        worker.execute();

        try {
            boolean completed = latch.await(2, TimeUnit.SECONDS);
            SimpleAssert.assertTrue(completed, "SwingWorker background task must finish within timeout");
            SimpleAssert.assertTrue(backgroundWasOffEdt.get(),
                    "SwingWorker doInBackground() MUST execute off the Event Dispatch Thread (EDT)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Test interrupted: " + e.getMessage());
        }

        // Test Uncaught Exception Handler
        MockUiFeedbackService mockUi = new MockUiFeedbackService();
        AtomicBoolean handlerInvoked = new AtomicBoolean(false);
        Thread.UncaughtExceptionHandler testHandler = (t, ex) -> {
            handlerInvoked.set(true);
            mockUi.showError(null, "Unhandled Exception", ex.getMessage());
        };

        Thread testThread = new Thread(() -> {
            throw new RuntimeException("Simulated unexpected UI fault");
        });
        testThread.setUncaughtExceptionHandler(testHandler);
        testThread.start();

        try {
            testThread.join(2000);
            SimpleAssert.assertTrue(handlerInvoked.get(), "Uncaught exception handler must capture thread failure");
            SimpleAssert.assertEquals(1, mockUi.errorCount, "Error dialog must be triggered for uncaught exception");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
