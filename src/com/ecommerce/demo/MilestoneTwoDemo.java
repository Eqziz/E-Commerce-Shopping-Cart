package com.ecommerce.demo;

import com.ecommerce.adapter.CryptoPaymentAdapter;
import com.ecommerce.adapter.PayPalPaymentAdapter;
import com.ecommerce.adapter.PaymentGateway;
import com.ecommerce.adapter.StripePaymentAdapter;
import com.ecommerce.builder.CustomProduct;
import com.ecommerce.builder.CustomProductBuilder;
import com.ecommerce.builder.ProductDirector;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.decorator.ExpressShippingDecorator;
import com.ecommerce.decorator.ExtendedWarrantyDecorator;
import com.ecommerce.decorator.FragileHandlingDecorator;
import com.ecommerce.decorator.GiftWrapDecorator;
import com.ecommerce.decorator.OrderItem;
import com.ecommerce.decorator.StandardOrderItem;
import com.ecommerce.facade.CheckoutFacade;
import com.ecommerce.factory.ProductCatalogRegistry;
import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.ProductType;
import com.ecommerce.observer.AnalyticsAuditObserver;
import com.ecommerce.observer.CustomerAlertObserver;
import com.ecommerce.observer.InventorySubject;
import com.ecommerce.observer.WarehouseInventoryObserver;
import com.ecommerce.strategy.BuyOneGetOneDiscountStrategy;
import com.ecommerce.strategy.DiscountResult;
import com.ecommerce.strategy.DiscountStrategy;
import com.ecommerce.strategy.FlatRateDiscountStrategy;
import com.ecommerce.strategy.PercentageDiscountStrategy;
import com.ecommerce.strategy.TieredLoyaltyDiscountStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * End-to-end integration test and demonstration suite for Milestone 2 (80% Scope).
 * Demonstrates the seamless collaboration of all 7 design patterns:
 * 1. Factory Method (Product Catalog) - AinurSarsenbai
 * 2. Builder (Custom Products) - Eqziz
 * 3. Observer (Stock & Price Alerts) - baktiyarovamereilim-lang
 * 4. Decorator (Value-Added Item Services) - baktiyarovamereilim-lang
 * 5. Strategy (Dynamic Discount Algorithms) - Eqziz
 * 6. Adapter (External Payment Gateways) - AinurSarsenbai
 * 7. Facade (Unified Checkout Orchestration) - AinurSarsenbai
 * 
 * @author Eqziz (Team Lead)
 */
public class MilestoneTwoDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("E-COMMERCE SHOPPING CART PLATFORM - MILESTONE 2 (80% SCOPE)");
        System.out.println("Team: Eqziz (Lead), baktiyarovamereilim-lang, AinurSarsenbai");
        System.out.println("================================================================================\n");

        // ---------------------------------------------------------------------
        // 1. FACTORY METHOD PATTERN (AinurSarsenbai)
        // ---------------------------------------------------------------------
        System.out.println("[1/7] FACTORY METHOD PATTERN: Catalog Production");
        System.out.println("--------------------------------------------------------------------------------");
        ProductCatalogRegistry catalog = new ProductCatalogRegistry();

        BaseProduct laptop = catalog.createProduct(ProductType.ELECTRONICS, "EL-100",
                "ProBook Ultra 16", "High-performance laptop with 165Hz display", 1299.99,
                Map.of("weightKg", 1.85, "dimensions", "35x24x1.6 cm"));

        BaseProduct hoodie = catalog.createProduct(ProductType.CLOTHING, "CL-200",
                "Developer Zip Hoodie", "Heavyweight fleece hoodie with inner pocket", 65.00,
                Map.of("size", "XL", "weightKg", 0.60));

        BaseProduct javaBook = catalog.createProduct(ProductType.BOOK, "BK-300",
                "Effective Java (3rd Edition)", "Definitive guide to Java best practices", 52.00,
                Map.of("author", "Joshua Bloch", "isbn", "978-0134685991", "weightKg", 0.75));

        BaseProduct cloudSub = catalog.createProduct(ProductType.DIGITAL, "DG-400",
                "Cloud Storage Pro 2TB", "1-Year Encrypted Cloud Backup Plan", 89.99,
                Map.of("downloadUrl", "https://cloud.ecommerce.com/activate", "licenseFormat", "CLOUD-2TB-2026"));

        System.out.println("Manufactured 4 standard products via Factory Method.\n");

        // ---------------------------------------------------------------------
        // 2. BUILDER PATTERN (Eqziz)
        // ---------------------------------------------------------------------
        System.out.println("[2/7] BUILDER PATTERN: Custom Hardware & Bundle Configuration");
        System.out.println("--------------------------------------------------------------------------------");
        ProductDirector director = new ProductDirector();
        CustomProductBuilder builder = new CustomProductBuilder();

        CustomProduct customWorkstation = director.constructStudentWorkstation(builder, "CUST-WS-01");
        System.out.println("Built preset workstation via ProductDirector:");
        System.out.println(customWorkstation);

        CustomProduct bespokeGamingRig = director.constructGamingPc(builder, "CUST-RIG-02");
        System.out.println("\nBuilt custom gaming rig via ProductDirector:");
        System.out.println(bespokeGamingRig);
        System.out.println("Manufactured 2 complex products via Builder.\n");

        // ---------------------------------------------------------------------
        // 3. OBSERVER PATTERN (baktiyarovamereilim-lang)
        // ---------------------------------------------------------------------
        System.out.println("[3/7] OBSERVER PATTERN: Real-Time Stock & Price Notification Dispatch");
        System.out.println("--------------------------------------------------------------------------------");
        InventorySubject inventory = new InventorySubject();

        CustomerAlertObserver customerObserver = new CustomerAlertObserver("CUST-101", "Alex Mercer", "alex@example.com");
        customerObserver.subscribeToProduct(laptop.getId());

        WarehouseInventoryObserver warehouseObserver = new WarehouseInventoryObserver("CENTRAL-HUB-01");
        AnalyticsAuditObserver auditObserver = new AnalyticsAuditObserver();

        inventory.attach(customerObserver);
        inventory.attach(warehouseObserver);
        inventory.attach(auditObserver);

        // Seed inventory stock
        inventory.registerProduct(laptop, 5);
        inventory.registerProduct(hoodie, 20);
        inventory.registerProduct(javaBook, 15);
        inventory.registerProduct(cloudSub, 999);
        inventory.registerProduct(customWorkstation, 4);
        inventory.registerProduct(bespokeGamingRig, 2);

        System.out.println("Testing stock warning and price drop events:");
        inventory.updateStock(bespokeGamingRig.getId(), 1); // Triggers low-stock warning
        inventory.updateProductPrice(laptop.getId(), 1199.99); // Triggers price drop alert
        System.out.println("Observer state transitions completed successfully.\n");

        // ---------------------------------------------------------------------
        // 4. STRATEGY PATTERN: Comparison of Discount Algorithms (Eqziz)
        // ---------------------------------------------------------------------
        System.out.println("[4/7] STRATEGY PATTERN: Evaluating Pluggable Discount Algorithms");
        System.out.println("--------------------------------------------------------------------------------");
        ShoppingCart cart = new ShoppingCart("CART-2026-M2", "CUST-101");
        cart.addItem(laptop, 1);
        cart.addItem(hoodie, 2);
        cart.addItem(javaBook, 2);
        cart.addItem(customWorkstation, 1);

        double baseSubtotal = cart.calculateSubtotal();
        System.out.printf("Cart Base Subtotal (before discounts): $%.2f%n", baseSubtotal);

        DiscountStrategy percentStrat = new PercentageDiscountStrategy("FALL15", 15.0);
        DiscountStrategy flatStrat = new FlatRateDiscountStrategy("SAVE50", 50.0, 500.0);
        DiscountStrategy loyaltyStrat = new TieredLoyaltyDiscountStrategy();
        DiscountStrategy bogoStrat = new BuyOneGetOneDiscountStrategy("ALL");

        System.out.println("Comparing strategy calculations:");
        System.out.println("  1. " + percentStrat.applyDiscount(baseSubtotal, cart.getItems(), "STANDARD"));
        System.out.println("  2. " + flatStrat.applyDiscount(baseSubtotal, cart.getItems(), "STANDARD"));
        System.out.println("  3. " + loyaltyStrat.applyDiscount(baseSubtotal, cart.getItems(), "GOLD"));
        System.out.println("  4. " + bogoStrat.applyDiscount(baseSubtotal, cart.getItems(), "STANDARD"));

        // Select Gold Loyalty Tier strategy for the checkout
        cart.setDiscountStrategy(loyaltyStrat);
        System.out.println("Strategy selected for order: " + cart.getDiscountStrategy().getStrategyName() + "\n");

        // ---------------------------------------------------------------------
        // 5. DECORATOR PATTERN: Value-Added Item Services (baktiyarovamereilim-lang)
        // ---------------------------------------------------------------------
        System.out.println("[5/7] DECORATOR PATTERN: Dynamic Line-Item Customization");
        System.out.println("--------------------------------------------------------------------------------");
        List<OrderItem> decoratedOrderItems = new ArrayList<>();

        // Decorating Laptop: Base + 24-Mo Extended Warranty + Fragile Handling
        OrderItem decoratedLaptop = new StandardOrderItem(laptop, 1);
        decoratedLaptop = new ExtendedWarrantyDecorator(decoratedLaptop, 24, 0.10);
        decoratedLaptop = new FragileHandlingDecorator(decoratedLaptop, 15.00);
        decoratedOrderItems.add(decoratedLaptop);

        // Decorating Hoodie: Base + Gift Wrapping with custom greeting
        OrderItem decoratedHoodie = new StandardOrderItem(hoodie, 2);
        decoratedHoodie = new GiftWrapDecorator(decoratedHoodie, "Happy Birthday Alex!");
        decoratedOrderItems.add(decoratedHoodie);

        // Decorating Workstation: Base + Express Air Courier Shipping
        OrderItem decoratedWorkstation = new StandardOrderItem(customWorkstation, 1);
        decoratedWorkstation = new ExpressShippingDecorator(decoratedWorkstation, 25.00);
        decoratedOrderItems.add(decoratedWorkstation);

        for (OrderItem doi : decoratedOrderItems) {
            System.out.println("Decorated: " + doi);
        }
        System.out.println("Decorators successfully chained and pricing aggregated.\n");

        // ---------------------------------------------------------------------
        // 6. ADAPTER PATTERN: Payment Gateway Integration (AinurSarsenbai)
        // ---------------------------------------------------------------------
        System.out.println("[6/7] ADAPTER PATTERN: Third-Party Payment Gateway Verification");
        System.out.println("--------------------------------------------------------------------------------");
        PaymentGateway stripeGateway = new StripePaymentAdapter("sk_live_test_stripe_secret_key");
        PaymentGateway payPalGateway = new PayPalPaymentAdapter("paypal_oauth_client_secret");
        PaymentGateway cryptoGateway = new CryptoPaymentAdapter();

        System.out.println("Available payment adapters:");
        System.out.println("  - " + stripeGateway.getGatewayName());
        System.out.println("  - " + payPalGateway.getGatewayName());
        System.out.println("  - " + cryptoGateway.getGatewayName());
        System.out.println("Adapter interfaces operational.\n");

        // ---------------------------------------------------------------------
        // 7. FACADE PATTERN: Unified Checkout Orchestration (AinurSarsenbai)
        // ---------------------------------------------------------------------
        System.out.println("[7/7] FACADE PATTERN: End-to-End Unified Checkout Execution");
        System.out.println("--------------------------------------------------------------------------------");
        Customer customer = new Customer("CUST-101", "Alex Mercer", "alex@example.com",
                "456 Tech Boulevard, Suite 800, Silicon Valley, CA", "GOLD");

        CheckoutFacade checkoutFacade = new CheckoutFacade(inventory, stripeGateway);

        // Process full order through Facade
        Order completedOrder = checkoutFacade.processCheckout(
                cart,
                customer,
                loyaltyStrat,
                stripeGateway,
                decoratedOrderItems
        );

        System.out.println("Checkout completed successfully. Order status: " + completedOrder.getStatus());
        System.out.println("Remaining inventory stock for laptop: " + inventory.getStock(laptop.getId()));
        System.out.println("Shopping cart item count after checkout: " + cart.getItemCount());

        System.out.println("\n================================================================================");
        System.out.println("Milestone 2 integration demonstration completed (80% Project Scope).");
        System.out.println("All 7 design patterns verified in coordination.");
        System.out.println("================================================================================");
    }
}
