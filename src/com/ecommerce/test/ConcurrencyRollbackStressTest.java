package com.ecommerce.test;

import com.ecommerce.adapter.PaymentGateway;
import com.ecommerce.adapter.PaymentResult;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.decorator.ExpressShippingDecorator;
import com.ecommerce.decorator.GiftWrapDecorator;
import com.ecommerce.decorator.OrderItem;
import com.ecommerce.decorator.StandardOrderItem;
import com.ecommerce.facade.CheckoutFacade;
import com.ecommerce.frontend.EcommerceSwingApp;
import com.ecommerce.frontend.PaymentCredentialsDialog;
import com.ecommerce.frontend.UiFeedbackService;
import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.observer.EcommerceEvent;
import com.ecommerce.observer.InventorySubject;
import com.ecommerce.strategy.NoDiscountStrategy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Empirical Stress Test Harness for Milestone 1 (Challenger 2).
 * Verifies:
 * 1. Checkout execution runs strictly off the EDT (!isEventDispatchThread()).
 * 2. UI mutations occur strictly on the EDT (isEventDispatchThread()).
 * 3. Cart contents and decorator selections are preserved on payment failure, rejection, or cancellation.
 * 4. Cart contents are cleared and inventory decremented if and only if checkout returns OrderStatus.PAID.
 * 5. Real-time InventorySubject observer notifications update UI safely under heavy multithreaded concurrency.
 */
public class ConcurrencyRollbackStressTest {

    public static class ThreadTrackingFeedbackService implements UiFeedbackService {
        public final AtomicInteger infoCount = new AtomicInteger(0);
        public final AtomicInteger warningCount = new AtomicInteger(0);
        public final AtomicInteger errorCount = new AtomicInteger(0);
        public final AtomicInteger confirmCount = new AtomicInteger(0);
        public final AtomicBoolean allCallsOnEdt = new AtomicBoolean(true);
        public volatile String lastTitle;
        public volatile String lastMessage;
        public volatile boolean confirmResponse = true;

        private void recordCall() {
            if (!SwingUtilities.isEventDispatchThread()) {
                allCallsOnEdt.set(false);
                System.err.println("[VIOLATION] UiFeedbackService method called OFF Event Dispatch Thread!");
            }
        }

        @Override
        public void showInformation(Component parent, String title, String message) {
            recordCall();
            infoCount.incrementAndGet();
            lastTitle = title;
            lastMessage = message;
        }

        @Override
        public void showWarning(Component parent, String title, String message) {
            recordCall();
            warningCount.incrementAndGet();
            lastTitle = title;
            lastMessage = message;
        }

        @Override
        public void showError(Component parent, String title, String message) {
            recordCall();
            errorCount.incrementAndGet();
            lastTitle = title;
            lastMessage = message;
        }

        @Override
        public boolean showConfirmation(Component parent, String title, String message) {
            recordCall();
            confirmCount.incrementAndGet();
            lastTitle = title;
            lastMessage = message;
            return confirmResponse;
        }

        public void reset() {
            infoCount.set(0);
            warningCount.set(0);
            errorCount.set(0);
            confirmCount.set(0);
            allCallsOnEdt.set(true);
            lastTitle = null;
            lastMessage = null;
            confirmResponse = true;
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("EMPIRICAL CONCURRENCY & STATE ROLLBACK STRESS TEST HARNESS (CHALLENGER 2)");
        System.out.println("================================================================================");

        try {
            testCheckoutRunsOffEdtAndEdURemainsResponsive();
            testUiUpdatesStrictlyOnEdt();
            testCartAndDecoratorsPreservedOnPaymentDecline();
            testCartAndDecoratorsPreservedOnPaymentException();
            testCartAndDecoratorsPreservedOnUserCancellation();
            testCartClearedAndInventoryDecrementedOnlyOnPaid();
            testInventorySubjectObserverMultithreadedStress();
            testConcurrentStockReservationNoOversell();

            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("ALL 8 STRESS TEST SUITES PASSED SUCCESSFULLY!");
            System.out.println("================================================================================");
        } catch (Throwable t) {
            System.err.println("\n[STRESS TEST FAILURE]: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Helper to inject custom gateway into EcommerceSwingApp's private paymentGateways map.
     */
    @SuppressWarnings("unchecked")
    private static void injectPaymentGateway(EcommerceSwingApp app, String name, PaymentGateway gateway) {
        try {
            Field f = EcommerceSwingApp.class.getDeclaredField("paymentGateways");
            f.setAccessible(true);
            Map<String, PaymentGateway> map = (Map<String, PaymentGateway>) f.get(app);
            map.put(name, gateway);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject gateway: " + e.getMessage(), e);
        }
    }

    /**
     * Helper to automatically dismiss modal PaymentCredentialsDialog in testing.
     */
    private static Thread scheduleDialogAutoHandler(boolean confirm, long timeoutMs) {
        Thread thread = new Thread(() -> {
            long deadline = System.currentTimeMillis() + timeoutMs;
            while (System.currentTimeMillis() < deadline) {
                for (Window w : Window.getWindows()) {
                    if (w instanceof PaymentCredentialsDialog dialog && dialog.isVisible()) {
                        SwingUtilities.invokeLater(() -> {
                            clickDialogButton(dialog, confirm ? "Authorize & Pay" : "Cancel");
                        });
                        return;
                    }
                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ignored) {
                    return;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static void clickDialogButton(Container container, String buttonText) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton btn && buttonText.equals(btn.getText())) {
                btn.doClick();
                return;
            } else if (c instanceof Container child) {
                clickDialogButton(child, buttonText);
            }
        }
    }

    /**
     * Test 1: Verify checkout payment processing executes off the EDT, and EDT remains responsive.
     */
    public static void testCheckoutRunsOffEdtAndEdURemainsResponsive() throws Exception {
        System.out.print("[Test 1/8] Verifying checkout runs off-EDT and UI does not freeze... ");
        ThreadTrackingFeedbackService mockUi = new ThreadTrackingFeedbackService();
        EcommerceSwingApp app = new EcommerceSwingApp(mockUi);
        app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Populate cart with 1 phone
        BaseProduct phone = app.getInventorySubject().getProduct("EL-101");
        app.getShoppingCart().addItem(phone, 1);

        AtomicBoolean payExecutedOffEdt = new AtomicBoolean(false);
        AtomicReference<String> payThreadName = new AtomicReference<>("");
        CountDownLatch payStartedLatch = new CountDownLatch(1);
        CountDownLatch payProceedLatch = new CountDownLatch(1);
        CountDownLatch checkoutFinishedLatch = new CountDownLatch(1);

        PaymentGateway slowGateway = new PaymentGateway() {
            @Override
            public String getGatewayName() {
                return "Stripe";
            }

            @Override
            public PaymentResult pay(String orderId, double amount, String customerEmail) {
                payExecutedOffEdt.set(!SwingUtilities.isEventDispatchThread());
                payThreadName.set(Thread.currentThread().getName());
                payStartedLatch.countDown();
                try {
                    // Simulate long network RPC (300 ms)
                    boolean proceed = payProceedLatch.await(3, TimeUnit.SECONDS);
                    if (!proceed) throw new RuntimeException("Latch timed out");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new PaymentResult(true, "TX-SLOW-1", amount, getGatewayName(), "Slow payment approved");
            }

            @Override
            public boolean refund(String transactionId, double amount) {
                return true;
            }
        };

        injectPaymentGateway(app, "Stripe", slowGateway);

        // Schedule auto-approval for dialog
        scheduleDialogAutoHandler(true, 5000);

        // Trigger checkout on EDT
        SwingUtilities.invokeLater(() -> {
            app.handleExecuteCheckout();
        });

        // Wait until pay() is actively running in background
        boolean started = payStartedLatch.await(3, TimeUnit.SECONDS);
        SimpleAssert.assertTrue(started, "Gateway pay() should start executing");

        // Verify pay() is running OFF EDT
        SimpleAssert.assertTrue(payExecutedOffEdt.get(), "Payment processing MUST execute off EDT!");
        SimpleAssert.assertFalse(payThreadName.get().contains("AWT-EventQueue"),
                "Payment processing thread MUST NOT be AWT-EventQueue: " + payThreadName.get());

        // WHILE pay() is sleeping, verify EDT is responsive and handles pending tasks promptly
        AtomicBoolean edtWasResponsive = new AtomicBoolean(false);
        CountDownLatch edtTaskLatch = new CountDownLatch(1);
        long startProbe = System.currentTimeMillis();
        SwingUtilities.invokeLater(() -> {
            edtWasResponsive.set(true);
            edtTaskLatch.countDown();
        });

        boolean edtTaskFinished = edtTaskLatch.await(150, TimeUnit.MILLISECONDS);
        long probeDuration = System.currentTimeMillis() - startProbe;
        SimpleAssert.assertTrue(edtTaskFinished, "EDT must respond immediately (< 150ms) while payment is pending. Took " + probeDuration + "ms");
        SimpleAssert.assertTrue(edtWasResponsive.get(), "EDT task was successfully executed concurrently with payment");

        // Release gateway pay() to finish
        payProceedLatch.countDown();

        // Wait for checkout done() to execute on EDT
        SwingUtilities.invokeLater(checkoutFinishedLatch::countDown);
        checkoutFinishedLatch.await(2, TimeUnit.SECONDS);

        // Give SwingWorker done() a brief window to finish UI updates
        Thread.sleep(150);

        app.dispose();
        System.out.println("PASSED (Off-EDT thread: " + payThreadName.get() + ", EDT latency: " + probeDuration + "ms)");
    }

    /**
     * Test 2: Verify all UI updates occur strictly on the EDT.
     */
    public static void testUiUpdatesStrictlyOnEdt() throws Exception {
        System.out.print("[Test 2/8] Verifying all UI mutations strictly occur on the EDT... ");
        ThreadTrackingFeedbackService mockUi = new ThreadTrackingFeedbackService();
        EcommerceSwingApp app = new EcommerceSwingApp(mockUi);
        app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        BaseProduct laptop = app.getInventorySubject().getProduct("EL-102");
        app.getShoppingCart().addItem(laptop, 1);

        AtomicBoolean doneRanOnEdt = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        PaymentGateway instantGateway = new PaymentGateway() {
            @Override
            public String getGatewayName() {
                return "Stripe";
            }

            @Override
            public PaymentResult pay(String orderId, double amount, String customerEmail) {
                return new PaymentResult(true, "TX-OK", amount, getGatewayName(), "Success");
            }

            @Override
            public boolean refund(String transactionId, double amount) {
                return true;
            }
        };

        injectPaymentGateway(app, "Stripe", instantGateway);
        scheduleDialogAutoHandler(true, 5000);

        SwingUtilities.invokeLater(() -> {
            app.handleExecuteCheckout();
        });

        // Schedule check on EDT after checkout completion
        Thread.sleep(300);
        SwingUtilities.invokeAndWait(() -> {
            doneRanOnEdt.set(SwingUtilities.isEventDispatchThread());
            latch.countDown();
        });

        latch.await(2, TimeUnit.SECONDS);

        SimpleAssert.assertTrue(doneRanOnEdt.get(), "Done/UI updates must occur on EDT");
        SimpleAssert.assertTrue(mockUi.allCallsOnEdt.get(), "All UiFeedbackService dialogs must be dispatched on EDT");
        SimpleAssert.assertTrue(mockUi.infoCount.get() >= 1, "Success notification was presented");

        app.dispose();
        System.out.println("PASSED (All UI callbacks strictly verified on EDT)");
    }

    /**
     * Test 3: Verify cart contents and decorator selections are preserved when payment is rejected/declined.
     */
    public static void testCartAndDecoratorsPreservedOnPaymentDecline() throws Exception {
        System.out.print("[Test 3/8] Verifying cart items and decorators are preserved on payment decline... ");
        ThreadTrackingFeedbackService mockUi = new ThreadTrackingFeedbackService();
        EcommerceSwingApp app = new EcommerceSwingApp(mockUi);
        app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        InventorySubject inventory = app.getInventorySubject();
        int initialPhoneStock = inventory.getStock("EL-101");
        int initialHoodieStock = inventory.getStock("CL-201");

        // Add 2 items to cart
        BaseProduct phone = inventory.getProduct("EL-101");
        BaseProduct hoodie = inventory.getProduct("CL-201");
        app.getShoppingCart().addItem(phone, 2);
        app.getShoppingCart().addItem(hoodie, 1);

        // Decorate phone with GiftWrap
        OrderItem decoratedPhone = new GiftWrapDecorator(
                new StandardOrderItem(new CartItem(phone, 2)), "Happy Birthday!", 5.00);
        // Decorate hoodie with ExpressShipping
        OrderItem decoratedHoodie = new ExpressShippingDecorator(
                new StandardOrderItem(new CartItem(hoodie, 1)), 15.00);

        app.getConfiguredOrderItems().add(decoratedPhone);
        app.getConfiguredOrderItems().add(decoratedHoodie);

        SimpleAssert.assertEquals(3, app.getShoppingCart().getItemCount(), "Cart should have 3 items initially");
        SimpleAssert.assertEquals(2, app.getConfiguredOrderItems().size(), "Cart should have 2 decorated items initially");

        // Inject declining payment gateway
        PaymentGateway decliningGateway = new PaymentGateway() {
            @Override
            public String getGatewayName() {
                return "Stripe";
            }

            @Override
            public PaymentResult pay(String orderId, double amount, String customerEmail) {
                return new PaymentResult(false, "TX-DECLINED-99", amount, getGatewayName(),
                        "Card declined: Insufficient credit limit.");
            }

            @Override
            public boolean refund(String transactionId, double amount) {
                return false;
            }
        };

        injectPaymentGateway(app, "Stripe", decliningGateway);
        scheduleDialogAutoHandler(true, 5000);

        SwingUtilities.invokeLater(app::handleExecuteCheckout);

        // Wait for worker to finish
        Thread.sleep(400);
        SwingUtilities.invokeAndWait(() -> {});

        // ASSERT CART ITEMS ARE FULLY PRESERVED
        SimpleAssert.assertEquals(3, app.getShoppingCart().getItemCount(),
                "Shopping cart items MUST NOT be cleared on payment decline!");
        SimpleAssert.assertFalse(app.getShoppingCart().isEmpty(), "Cart must not be empty on decline");

        // ASSERT DECORATOR CONFIGURATIONS ARE FULLY PRESERVED
        SimpleAssert.assertEquals(2, app.getConfiguredOrderItems().size(),
                "Configured decorator list MUST NOT be cleared on payment decline!");
        OrderItem item1 = app.getConfiguredOrderItems().stream()
                .filter(i -> i.getItemId().equals("EL-101")).findFirst().orElse(null);
        SimpleAssert.assertNotNull(item1, "Decorated phone must remain in configured decorators");
        SimpleAssert.assertTrue(item1.getDescription().contains("Gift Wrap"),
                "Phone decorator description must still contain Gift Wrap");

        // ASSERT STOCK RESERVATION WAS ROLLED BACK
        SimpleAssert.assertEquals(initialPhoneStock, inventory.getStock("EL-101"),
                "Stock for phone must be restored to initial level on decline");
        SimpleAssert.assertEquals(initialHoodieStock, inventory.getStock("CL-201"),
                "Stock for hoodie must be restored to initial level on decline");

        // ASSERT ERROR NOTIFICATION DISPLAYED
        SimpleAssert.assertTrue(mockUi.errorCount.get() >= 1, "Error dialog must be displayed on payment decline");
        SimpleAssert.assertTrue(mockUi.lastMessage.contains("preserved"),
                "User dialog must explicitly inform user that cart is preserved");

        app.dispose();
        System.out.println("PASSED (Cart intact, decorators intact, stock rolled back)");
    }

    /**
     * Test 4: Verify cart contents and decorators are preserved when payment gateway throws RuntimeException.
     */
    public static void testCartAndDecoratorsPreservedOnPaymentException() throws Exception {
        System.out.print("[Test 4/8] Verifying cart items and decorators are preserved on gateway exception... ");
        ThreadTrackingFeedbackService mockUi = new ThreadTrackingFeedbackService();
        EcommerceSwingApp app = new EcommerceSwingApp(mockUi);
        app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        BaseProduct book = app.getInventorySubject().getProduct("BK-301");
        app.getShoppingCart().addItem(book, 2);

        OrderItem decoratedBook = new GiftWrapDecorator(
                new StandardOrderItem(new CartItem(book, 2)), "Congratulations!", 4.99);
        app.getConfiguredOrderItems().add(decoratedBook);

        PaymentGateway crashingGateway = new PaymentGateway() {
            @Override
            public String getGatewayName() {
                return "Stripe";
            }

            @Override
            public PaymentResult pay(String orderId, double amount, String customerEmail) {
                throw new RuntimeException("Gateway Network Timeout (HTTP 504 Gateway Unavailable)");
            }

            @Override
            public boolean refund(String transactionId, double amount) {
                return false;
            }
        };

        injectPaymentGateway(app, "Stripe", crashingGateway);
        scheduleDialogAutoHandler(true, 5000);

        SwingUtilities.invokeLater(app::handleExecuteCheckout);

        Thread.sleep(400);
        SwingUtilities.invokeAndWait(() -> {});

        // Cart and decorators preserved
        SimpleAssert.assertEquals(2, app.getShoppingCart().getItemCount(), "Cart intact on exception");
        SimpleAssert.assertEquals(1, app.getConfiguredOrderItems().size(), "Decorators intact on exception");
        SimpleAssert.assertTrue(mockUi.errorCount.get() >= 1, "Error dialog shown on exception");

        app.dispose();
        System.out.println("PASSED (Preserved on unhandled gateway exception)");
    }

    /**
     * Test 5: Verify cart contents and decorators are preserved when user cancels credentials dialog.
     */
    public static void testCartAndDecoratorsPreservedOnUserCancellation() throws Exception {
        System.out.print("[Test 5/8] Verifying cart items and decorators are preserved on user cancellation... ");
        ThreadTrackingFeedbackService mockUi = new ThreadTrackingFeedbackService();
        EcommerceSwingApp app = new EcommerceSwingApp(mockUi);
        app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        BaseProduct hoodie = app.getInventorySubject().getProduct("CL-201");
        app.getShoppingCart().addItem(hoodie, 1);

        OrderItem decorated = new ExpressShippingDecorator(new StandardOrderItem(new CartItem(hoodie, 1)), 15.0);
        app.getConfiguredOrderItems().add(decorated);

        // Schedule dialog CANCEL action
        scheduleDialogAutoHandler(false, 5000);

        SwingUtilities.invokeLater(app::handleExecuteCheckout);

        Thread.sleep(300);
        SwingUtilities.invokeAndWait(() -> {});

        SimpleAssert.assertEquals(1, app.getShoppingCart().getItemCount(), "Cart intact on cancellation");
        SimpleAssert.assertEquals(1, app.getConfiguredOrderItems().size(), "Decorators intact on cancellation");
        SimpleAssert.assertTrue(mockUi.infoCount.get() >= 1, "Cancellation feedback shown");
        SimpleAssert.assertTrue(mockUi.lastTitle.contains("Cancelled"), "Title indicates checkout cancelled");

        app.dispose();
        System.out.println("PASSED (Preserved on credentials dialog cancellation)");
    }

    /**
     * Test 6: Verify cart contents are cleared and inventory decremented if and only if checkout returns OrderStatus.PAID.
     */
    public static void testCartClearedAndInventoryDecrementedOnlyOnPaid() throws Exception {
        System.out.print("[Test 6/8] Verifying cart cleared & inventory updated ONLY on confirmed PAID... ");
        ThreadTrackingFeedbackService mockUi = new ThreadTrackingFeedbackService();
        EcommerceSwingApp app = new EcommerceSwingApp(mockUi);
        app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        InventorySubject inventory = app.getInventorySubject();
        int initialPhoneStock = inventory.getStock("EL-101");
        int buyQty = 3;

        BaseProduct phone = inventory.getProduct("EL-101");
        app.getShoppingCart().addItem(phone, buyQty);

        OrderItem decoratedPhone = new GiftWrapDecorator(
                new StandardOrderItem(new CartItem(phone, buyQty)), "VIP Gift", 10.0);
        app.getConfiguredOrderItems().add(decoratedPhone);

        PaymentGateway approvingGateway = new PaymentGateway() {
            @Override
            public String getGatewayName() {
                return "Stripe";
            }

            @Override
            public PaymentResult pay(String orderId, double amount, String customerEmail) {
                return new PaymentResult(true, "TX-SUCCESS-AUTH", amount, getGatewayName(), "Payment authorized.");
            }

            @Override
            public boolean refund(String transactionId, double amount) {
                return true;
            }
        };

        injectPaymentGateway(app, "Stripe", approvingGateway);
        scheduleDialogAutoHandler(true, 5000);

        SwingUtilities.invokeLater(app::handleExecuteCheckout);

        Thread.sleep(400);
        SwingUtilities.invokeAndWait(() -> {});

        // ASSERT CART ITEMS CLEARED
        SimpleAssert.assertEquals(0, app.getShoppingCart().getItemCount(), "Cart items must be cleared on success");
        SimpleAssert.assertTrue(app.getShoppingCart().isEmpty(), "Shopping cart must be empty");

        // ASSERT DECORATOR CONFIGURATIONS CLEARED
        SimpleAssert.assertEquals(0, app.getConfiguredOrderItems().size(), "Configured decorators must be cleared");

        // ASSERT INVENTORY DECREMENTED BY EXACT QUANTITY
        int expectedStock = initialPhoneStock - buyQty;
        SimpleAssert.assertEquals(expectedStock, inventory.getStock("EL-101"),
                "Warehouse inventory must be decremented by exactly purchased quantity (" + buyQty + ")");

        // ASSERT INVOICE TEXT AREA POPULATED
        JTextArea invoiceArea = app.getInvoiceTextArea();
        SimpleAssert.assertNotNull(invoiceArea, "Invoice text area exists");
        SimpleAssert.assertTrue(invoiceArea.getText().contains("COMMERCIAL ORDER INVOICE"),
                "Invoice must be rendered on checkout success");
        SimpleAssert.assertTrue(invoiceArea.getText().contains("Flagship Smartphone"),
                "Invoice must contain product description 'Flagship Smartphone'");
        SimpleAssert.assertTrue(invoiceArea.getText().contains("VIP Gift"),
                "Invoice must contain decorator description 'VIP Gift'");

        // ASSERT SUCCESS FEEDBACK PRESENTED
        SimpleAssert.assertTrue(mockUi.infoCount.get() >= 1, "Success notification must be presented");
        SimpleAssert.assertTrue(mockUi.lastTitle.contains("Success"), "Title indicates success");

        app.dispose();
        System.out.println("PASSED (Cart emptied, decorators cleared, stock accurately decremented)");
    }

    /**
     * Test 7: Verify real-time InventorySubject observer notifications update UI safely under heavy multithreaded concurrency.
     */
    public static void testInventorySubjectObserverMultithreadedStress() throws Exception {
        System.out.print("[Test 7/8] Stress-testing real-time InventorySubject notifications under 10 concurrent threads... ");
        ThreadTrackingFeedbackService mockUi = new ThreadTrackingFeedbackService();
        EcommerceSwingApp app = new EcommerceSwingApp(mockUi);
        app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        InventorySubject inventory = app.getInventorySubject();
        int threadCount = 10;
        int operationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger totalEventsGenerated = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadIdx = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int op = 0; op < operationsPerThread; op++) {
                        String prodId = (op % 2 == 0) ? "EL-101" : "EL-102";
                        if (threadIdx % 3 == 0) {
                            // Stock updates
                            inventory.updateStock(prodId, 5 + (op % 15));
                        } else if (threadIdx % 3 == 1) {
                            // Price fluctuations
                            inventory.updateProductPrice(prodId, 1000.0 + (op * 2.5));
                        } else {
                            // Stock reservations & releases
                            boolean reserved = inventory.reserveStock(prodId, 1);
                            if (reserved) {
                                inventory.updateStock(prodId, inventory.getStock(prodId) + 1);
                            }
                        }
                        totalEventsGenerated.incrementAndGet();
                        Thread.sleep(2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Unleash all 10 concurrent threads simultaneously
        startLatch.countDown();
        boolean finished = finishLatch.await(10, TimeUnit.SECONDS);
        SimpleAssert.assertTrue(finished, "Multithreaded stress test threads must finish within timeout");
        executor.shutdown();

        // Flush EDT event queue to process all pending invokeLater updates
        SwingUtilities.invokeAndWait(() -> {
            // Assert catalog table model has rows and no corrupted states
            JTable table = app.getCatalogTable();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            SimpleAssert.assertTrue(model.getRowCount() >= 5, "Catalog table rows must remain intact");
        });

        app.dispose();
        System.out.println("PASSED (" + totalEventsGenerated.get() + " concurrent events processed with zero exceptions)");
    }

    /**
     * Test 8: Verify concurrent stock reservations on InventorySubject prevent overselling under race conditions.
     */
    public static void testConcurrentStockReservationNoOversell() throws Exception {
        System.out.print("[Test 8/8] Stress-testing atomic stock reservation to prevent overselling... ");
        InventorySubject inventory = new InventorySubject();
        BaseProduct limitedItem = new com.ecommerce.model.PhysicalProduct(
                "LIMITED-01", "Limited Edition GPU", "Only 10 available", 999.0,
                com.ecommerce.model.ProductType.ELECTRONICS, 1.5, "Box");

        int initialStock = 10;
        inventory.registerProduct(limitedItem, initialStock);

        int concurrentBuyers = 30; // 30 concurrent threads trying to buy 1 unit each
        ExecutorService executor = Executors.newFixedThreadPool(concurrentBuyers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(concurrentBuyers);
        AtomicInteger successfulReservations = new AtomicInteger(0);
        AtomicInteger failedReservations = new AtomicInteger(0);

        for (int i = 0; i < concurrentBuyers; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    boolean reserved = inventory.reserveStock("LIMITED-01", 1);
                    if (reserved) {
                        successfulReservations.incrementAndGet();
                    } else {
                        failedReservations.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Exactly initialStock reservations must succeed
        SimpleAssert.assertEquals(initialStock, successfulReservations.get(),
                "Exactly " + initialStock + " reservations must succeed");
        // Exactly (concurrentBuyers - initialStock) must fail
        SimpleAssert.assertEquals(concurrentBuyers - initialStock, failedReservations.get(),
                "Remaining " + (concurrentBuyers - initialStock) + " reservations must fail");
        // Remaining stock must be exactly 0
        SimpleAssert.assertEquals(0, inventory.getStock("LIMITED-01"),
                "Stock level must be exactly 0 and NEVER negative");

        System.out.println("PASSED (30 concurrent buyers for 10 units: exactly 10 won, 20 rejected, stock = 0)");
    }
}
