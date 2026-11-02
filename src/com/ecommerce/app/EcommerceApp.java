package com.ecommerce.app;

import com.ecommerce.adapter.CryptoPaymentAdapter;
import com.ecommerce.adapter.PayPalPaymentAdapter;
import com.ecommerce.adapter.PaymentGateway;
import com.ecommerce.adapter.StripePaymentAdapter;
import com.ecommerce.builder.CustomProduct;
import com.ecommerce.builder.CustomProductBuilder;
import com.ecommerce.builder.ProductDirector;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.decorator.OrderItem;
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
import com.ecommerce.strategy.DiscountStrategy;
import com.ecommerce.strategy.FlatRateDiscountStrategy;
import com.ecommerce.strategy.NoDiscountStrategy;
import com.ecommerce.strategy.PercentageDiscountStrategy;
import com.ecommerce.strategy.TieredLoyaltyDiscountStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Interactive Console CLI Application for the E-Commerce Shopping Cart System.
 * Coordinates all 7 classical design patterns within a unified, interactive terminal interface.
 * 
 * @author Eqziz (Team Lead)
 */
public class EcommerceApp {
    private final ProductCatalogRegistry catalogRegistry;
    private final ProductDirector productDirector;
    private final CustomProductBuilder customProductBuilder;
    private final InventorySubject inventorySubject;
    private final ShoppingCart shoppingCart;
    private final Customer currentCustomer;
    private final List<OrderItem> configuredOrderItems;
    private final List<Order> orderHistory;

    // Observers
    private final CustomerAlertObserver customerAlertObserver;
    private final WarehouseInventoryObserver warehouseObserver;
    private final AnalyticsAuditObserver auditObserver;

    // Payment Gateways
    private final Map<Integer, PaymentGateway> paymentGateways;
    private final CheckoutFacade checkoutFacade;

    public EcommerceApp() {
        this.catalogRegistry = new ProductCatalogRegistry();
        this.productDirector = new ProductDirector();
        this.customProductBuilder = new CustomProductBuilder();
        this.inventorySubject = new InventorySubject();
        this.shoppingCart = new ShoppingCart("CART-LIVE-001", "CUST-CURRENT");
        this.currentCustomer = new Customer("CUST-CURRENT", "Nodir Mukhammedov",
                "nodir@university.edu", "Tashkent, Universitet ko'chasi 4", "GOLD");
        this.configuredOrderItems = new ArrayList<>();
        this.orderHistory = new ArrayList<>();

        // Initialize Observers
        this.customerAlertObserver = new CustomerAlertObserver(currentCustomer.getId(),
                currentCustomer.getFullName(), currentCustomer.getEmail());
        this.warehouseObserver = new WarehouseInventoryObserver("TASHKENT-CENTRAL");
        this.auditObserver = new AnalyticsAuditObserver();

        this.inventorySubject.attach(customerAlertObserver);
        this.inventorySubject.attach(warehouseObserver);
        this.inventorySubject.attach(auditObserver);

        // Seed initial store inventory
        seedInitialStoreData();

        // Initialize Payment Gateways (Adapters)
        this.paymentGateways = new HashMap<>();
        this.paymentGateways.put(1, new StripePaymentAdapter("sk_live_stripe_default_key"));
        this.paymentGateways.put(2, new PayPalPaymentAdapter("paypal_client_secret_live"));
        this.paymentGateways.put(3, new CryptoPaymentAdapter("0x71C8FB9bC2525D4f28E8C3411bA67C429188aB29", "USDT"));

        this.checkoutFacade = new CheckoutFacade(inventorySubject, paymentGateways.get(1));
    }

    private void seedInitialStoreData() {
        BaseProduct phone = catalogRegistry.createProduct(ProductType.ELECTRONICS, "EL-101",
                "Galaxy Ultra Pro", "Flagship Smartphone with Dynamic AMOLED", 1199.99,
                Map.of("weightKg", 0.23, "dimensions", "16x7.5x0.8 cm"));

        BaseProduct laptop = catalogRegistry.createProduct(ProductType.ELECTRONICS, "EL-102",
                "MacBook Pro 16", "High-performance laptop M3 Max chip", 2499.00,
                Map.of("weightKg", 2.15, "dimensions", "35x24x1.7 cm"));

        BaseProduct hoodie = catalogRegistry.createProduct(ProductType.CLOTHING, "CL-201",
                "University Tech Hoodie", "Heavyweight fleece hoodie embroidered", 55.00,
                Map.of("size", "L", "weightKg", 0.50));

        BaseProduct javaBook = catalogRegistry.createProduct(ProductType.BOOK, "BK-301",
                "Clean Code", "Handbook of Agile Software Craftsmanship", 45.00,
                Map.of("author", "Robert C. Martin", "isbn", "978-0132350884", "weightKg", 0.65));

        BaseProduct jetbrainsLicense = catalogRegistry.createProduct(ProductType.DIGITAL, "DG-401",
                "All Products Pack", "1-Year JetBrains Developer License", 289.00,
                Map.of("downloadUrl", "https://jetbrains.com/all", "licenseFormat", "ALL-PACK-2026"));

        inventorySubject.registerProduct(phone, 8);
        inventorySubject.registerProduct(laptop, 5);
        inventorySubject.registerProduct(hoodie, 25);
        inventorySubject.registerProduct(javaBook, 15);
        inventorySubject.registerProduct(jetbrainsLicense, 9999);

        // Subscribe current customer to phone price alerts
        customerAlertObserver.subscribeToProduct(phone.getId());
    }

    public void run() {
        boolean exit = false;
        while (!exit) {
            printMainMenu();
            int choice = ConsoleUiHelper.promptInt("Select an option", 1, 8);
            switch (choice) {
                case 1 -> System.out.println("Catalog viewing module...");
                case 2 -> System.out.println("Custom builder wizard...");
                case 3 -> System.out.println("Cart management...");
                case 4 -> System.out.println("Decorator selection...");
                case 5 -> System.out.println("Checkout execution...");
                case 6 -> System.out.println("Observer live status...");
                case 7 -> System.out.println("System audit log...");
                case 8 -> {
                    System.out.println("Exiting E-Commerce Shopping Cart System. Goodbye.");
                    exit = true;
                }
            }
        }
    }

    private void printMainMenu() {
        ConsoleUiHelper.printHeader("E-Commerce Shopping Cart Platform - Main Menu");
        System.out.printf("Active Customer: %s | Tier: %s | Cart Items: %d%n",
                currentCustomer.getFullName(), currentCustomer.getLoyaltyTier(), shoppingCart.getItemCount());
        ConsoleUiHelper.printThinDivider();
        System.out.println("1. Browse Catalog (Factory Method)");
        System.out.println("2. Build Custom Computer / Gift Bundle (Builder Pattern)");
        System.out.println("3. View & Modify Shopping Cart");
        System.out.println("4. Configure Value-Added Services (Decorator Pattern)");
        System.out.println("5. Checkout Order (Facade, Strategy & Adapter Patterns)");
        System.out.println("6. Inventory & Price Alert Stream (Observer Pattern)");
        System.out.println("7. View Store Audit Log & BI Metrics");
        System.out.println("8. Exit Application");
        ConsoleUiHelper.printThinDivider();
    }

    public static void main(String[] args) {
        EcommerceApp app = new EcommerceApp();
        app.run();
    }
}
