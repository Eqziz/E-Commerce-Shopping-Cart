package com.ecommerce.app;

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
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.ProductType;
import com.ecommerce.observer.AnalyticsAuditObserver;
import com.ecommerce.observer.CustomerAlertObserver;
import com.ecommerce.observer.EcommerceEvent;
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
import java.util.UUID;

/**
 * Main Interactive Console CLI Application for the E-Commerce Shopping Cart System.
 * Coordinates all 7 classical design patterns within a unified, interactive terminal interface.
 * 
 * @author Eqziz (Team Lead)
 * @author AinurSarsenbai (Catalog, Builder, Facade & Adapter Integration)
 * @author baktiyarovamereilim-lang (Cart, Decorator & Observer Integration)
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
    private final List<BaseProduct> catalogItems;

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
        this.catalogItems = new ArrayList<>();

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

        registerCatalogItem(phone, 8);
        registerCatalogItem(laptop, 5);
        registerCatalogItem(hoodie, 25);
        registerCatalogItem(javaBook, 15);
        registerCatalogItem(jetbrainsLicense, 9999);

        // Subscribe current customer to phone price alerts
        customerAlertObserver.subscribeToProduct(phone.getId());
    }

    private void registerCatalogItem(BaseProduct product, int stock) {
        catalogItems.add(product);
        inventorySubject.registerProduct(product, stock);
    }

    public void run() {
        boolean exit = false;
        while (!exit) {
            printMainMenu();
            int choice = ConsoleUiHelper.promptInt("Select an option", 1, 8);
            switch (choice) {
                case 1 -> handleBrowseCatalog();
                case 2 -> handleBuildCustomProduct();
                case 3 -> handleViewCart();
                case 4 -> handleConfigureDecorators();
                case 5 -> handleCheckout();
                case 6 -> handleViewObserverStream();
                case 7 -> handleViewAuditLog();
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

    private void handleBrowseCatalog() {
        ConsoleUiHelper.printHeader("Product Catalog - Factory Method Architecture");
        System.out.println("All items instantiated polymorphically through Category Factories:");
        ConsoleUiHelper.printThinDivider();

        for (int i = 0; i < catalogItems.size(); i++) {
            BaseProduct p = catalogItems.get(i);
            int stock = inventorySubject.getStock(p.getId());
            System.out.printf("  %d. [%-11s] %-26s | SKU: %-6s | Price: $%7.2f | Stock: %d%n",
                    i + 1, p.getProductType(), p.getName(), p.getId(), p.getPrice(), stock);
            System.out.printf("      Details: %s%n", p.getDescription());
        }
        ConsoleUiHelper.printThinDivider();
        System.out.printf("  0. Return to Main Menu%n");

        int choice = ConsoleUiHelper.promptInt("Select a product to add to cart (or 0 to return)", 0, catalogItems.size());
        if (choice == 0) return;

        BaseProduct selected = catalogItems.get(choice - 1);
        int availableStock = inventorySubject.getStock(selected.getId());
        if (availableStock <= 0) {
            System.out.println("Sorry, this item is currently out of stock.");
            return;
        }

        int qty = ConsoleUiHelper.promptInt("Enter quantity", 1, availableStock);
        shoppingCart.addItem(selected, qty);
        System.out.printf("Added %d unit(s) of %s to your cart.%n", qty, selected.getName());
    }

    private void handleBuildCustomProduct() {
        ConsoleUiHelper.printHeader("Custom Product Assembly - Builder Pattern");
        System.out.println("1. Pre-configured Gaming Rig (Apex Titan Gaming PC)");
        System.out.println("2. Pre-configured Student Workstation (EduPro Workstation)");
        System.out.println("3. Pre-configured VIP Gift Hamper (Executive Tech Hamper)");
        System.out.println("4. Bespoke Custom PC (Step-by-step Component Selection)");
        System.out.println("0. Return to Main Menu");
        ConsoleUiHelper.printThinDivider();

        int choice = ConsoleUiHelper.promptInt("Select build option", 0, 4);
        if (choice == 0) return;

        CustomProduct builtProduct = null;
        String customSku = "CUST-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        switch (choice) {
            case 1 -> builtProduct = productDirector.constructGamingPc(customProductBuilder, customSku);
            case 2 -> builtProduct = productDirector.constructStudentWorkstation(customProductBuilder, customSku);
            case 3 -> builtProduct = productDirector.constructLuxuryGiftBundle(customProductBuilder, customSku);
            case 4 -> builtProduct = assembleBespokePc(customSku);
        }

        if (builtProduct != null) {
            ConsoleUiHelper.printSubHeader("Custom Build Summary");
            System.out.println(builtProduct);
            registerCatalogItem(builtProduct, 3);

            boolean addToCart = ConsoleUiHelper.promptBoolean("Add this custom build to your shopping cart?");
            if (addToCart) {
                shoppingCart.addItem(builtProduct, 1);
                System.out.println("Custom configuration added to shopping cart.");
            }
        }
    }

    private CustomProduct assembleBespokePc(String customSku) {
        ConsoleUiHelper.printHeader("Bespoke PC Configuration Wizard");
        String name = ConsoleUiHelper.promptString("Enter your custom PC build name", "Custom Power Rig");

        customProductBuilder.reset()
                .setId(customSku)
                .setName(name)
                .setProductType(ProductType.CUSTOM_HARDWARE)
                .setBasePrice(350.00);

        System.out.println("Select Processor (CPU):");
        System.out.println("  1. AMD Ryzen 5 7600 (+$220.00)");
        System.out.println("  2. AMD Ryzen 7 7800X3D (+$380.00)");
        System.out.println("  3. Intel Core i9-14900K (+$540.00)");
        int cpuChoice = ConsoleUiHelper.promptInt("CPU Choice", 1, 3);
        switch (cpuChoice) {
            case 1 -> customProductBuilder.setProcessor("AMD Ryzen 5 7600 6-Core", 220.00);
            case 2 -> customProductBuilder.setProcessor("AMD Ryzen 7 7800X3D 8-Core V-Cache", 380.00);
            case 3 -> customProductBuilder.setProcessor("Intel Core i9-14900K 24-Core", 540.00);
        }

        System.out.println("Select Memory (RAM):");
        System.out.println("  1. 16GB DDR5 5600MHz (+$75.00)");
        System.out.println("  2. 32GB DDR5 6000MHz RGB (+$130.00)");
        System.out.println("  3. 64GB DDR5 6400MHz Low-Latency (+$240.00)");
        int ramChoice = ConsoleUiHelper.promptInt("RAM Choice", 1, 3);
        switch (ramChoice) {
            case 1 -> customProductBuilder.setMemoryRam("16GB DDR5-5600MHz", 75.00);
            case 2 -> customProductBuilder.setMemoryRam("32GB DDR5-6000MHz RGB", 130.00);
            case 3 -> customProductBuilder.setMemoryRam("64GB DDR5-6400MHz Low-Latency", 240.00);
        }

        System.out.println("Select Graphics Card (GPU):");
        System.out.println("  1. NVIDIA GeForce RTX 4060 8GB (+$299.00)");
        System.out.println("  2. NVIDIA GeForce RTX 4070 Ti Super 16GB (+$799.00)");
        System.out.println("  3. NVIDIA GeForce RTX 4090 24GB (+$1699.00)");
        int gpuChoice = ConsoleUiHelper.promptInt("GPU Choice", 1, 3);
        switch (gpuChoice) {
            case 1 -> customProductBuilder.setGraphicsCard("NVIDIA GeForce RTX 4060 8GB", 299.00);
            case 2 -> customProductBuilder.setGraphicsCard("NVIDIA GeForce RTX 4070 Ti Super 16GB", 799.00);
            case 3 -> customProductBuilder.setGraphicsCard("NVIDIA GeForce RTX 4090 24GB", 1699.00);
        }

        System.out.println("Select NVMe SSD Storage:");
        System.out.println("  1. 1TB Gen4 NVMe SSD (+$85.00)");
        System.out.println("  2. 2TB Samsung 990 Pro Gen4 SSD (+$175.00)");
        System.out.println("  3. 4TB High-Speed Enterprise SSD (+$320.00)");
        int ssdChoice = ConsoleUiHelper.promptInt("Storage Choice", 1, 3);
        switch (ssdChoice) {
            case 1 -> customProductBuilder.setStorage("1TB Gen4 NVMe SSD", 85.00);
            case 2 -> customProductBuilder.setStorage("2TB Samsung 990 Pro Gen4 SSD", 175.00);
            case 3 -> customProductBuilder.setStorage("4TB High-Speed Enterprise SSD", 320.00);
        }

        customProductBuilder.setPowerSupply("850W 80+ Gold Fully Modular", 135.00);

        System.out.println("Select Operating System:");
        System.out.println("  1. Windows 11 Home (+$119.00)");
        System.out.println("  2. Windows 11 Pro (+$159.00)");
        System.out.println("  3. Ubuntu Linux LTS (+$0.00)");
        int osChoice = ConsoleUiHelper.promptInt("OS Choice", 1, 3);
        switch (osChoice) {
            case 1 -> customProductBuilder.setOperatingSystem("Windows 11 Home 64-Bit", 119.00);
            case 2 -> customProductBuilder.setOperatingSystem("Windows 11 Pro 64-Bit", 159.00);
            case 3 -> customProductBuilder.setOperatingSystem("Ubuntu Linux 24.04 LTS", 0.00);
        }

        customProductBuilder.setPackagingType("Reinforced Custom Wooden Crate", 40.00);
        return customProductBuilder.build();
    }

    private void handleViewCart() {
        shoppingCart.printCartSummary();
        if (shoppingCart.isEmpty()) return;

        System.out.println("Options:");
        System.out.println("  1. Update Item Quantity");
        System.out.println("  2. Remove Item from Cart");
        System.out.println("  3. Clear Entire Cart");
        System.out.println("  0. Back to Main Menu");

        int choice = ConsoleUiHelper.promptInt("Select action", 0, 3);
        switch (choice) {
            case 1 -> {
                List<CartItem> items = shoppingCart.getItems();
                int itemIdx = ConsoleUiHelper.promptInt("Select item index", 1, items.size());
                CartItem item = items.get(itemIdx - 1);
                int newQty = ConsoleUiHelper.promptInt("Enter new quantity (0 to remove)", 0, 50);
                shoppingCart.updateQuantity(item.getProduct().getId(), newQty);
                System.out.println("Cart updated successfully.");
            }
            case 2 -> {
                List<CartItem> items = shoppingCart.getItems();
                int itemIdx = ConsoleUiHelper.promptInt("Select item index to remove", 1, items.size());
                CartItem item = items.get(itemIdx - 1);
                shoppingCart.removeItem(item.getProduct().getId());
                System.out.printf("Removed %s from cart.%n", item.getProduct().getName());
            }
            case 3 -> {
                shoppingCart.clear();
                System.out.println("Shopping cart cleared.");
            }
        }
    }

    private void handleConfigureDecorators() {
        ConsoleUiHelper.printHeader("Value-Added Service Decorator Configuration");
        if (shoppingCart.isEmpty()) {
            System.out.println("Your cart is empty. Add items before configuring decorators.");
            return;
        }

        List<CartItem> items = shoppingCart.getItems();
        System.out.println("Select a cart line item to decorate:");
        for (int i = 0; i < items.size(); i++) {
            CartItem ci = items.get(i);
            System.out.printf("  %d. %s (Qty: %d) - Subtotal: $%.2f%n",
                    i + 1, ci.getProduct().getName(), ci.getQuantity(), ci.getSubtotal());
        }
        System.out.println("  0. Finish & Return");

        int selection = ConsoleUiHelper.promptInt("Select item", 0, items.size());
        if (selection == 0) return;

        CartItem target = items.get(selection - 1);
        OrderItem currentDecorator = new StandardOrderItem(target);

        boolean decorating = true;
        while (decorating) {
            ConsoleUiHelper.printSubHeader("Current Item State");
            System.out.println("  " + currentDecorator);
            System.out.println("Add a Decorator Service:");
            System.out.println("  1. Gift Wrapping with Greeting Card (+$4.99/unit)");
            System.out.println("  2. 24-Month Extended Warranty (10% of base price)");
            System.out.println("  3. Priority Air Express Delivery (+$15.00 flat)");
            System.out.println("  4. Fragile Shock-Proof Protective Handling (+$10.00 flat)");
            System.out.println("  5. Save & Apply Decorated Line Item");
            System.out.println("  0. Cancel Decorating");

            int opt = ConsoleUiHelper.promptInt("Select option", 0, 5);
            switch (opt) {
                case 1 -> {
                    String msg = ConsoleUiHelper.promptString("Enter gift card greeting message", "Best wishes from Nodir!");
                    currentDecorator = new GiftWrapDecorator(currentDecorator, msg);
                    System.out.println("Gift wrap decorator attached.");
                }
                case 2 -> {
                    currentDecorator = new ExtendedWarrantyDecorator(currentDecorator, 24, 0.10);
                    System.out.println("Extended warranty decorator attached.");
                }
                case 3 -> {
                    currentDecorator = new ExpressShippingDecorator(currentDecorator, 15.00);
                    System.out.println("Express shipping decorator attached.");
                }
                case 4 -> {
                    currentDecorator = new FragileHandlingDecorator(currentDecorator, 10.00);
                    System.out.println("Fragile handling decorator attached.");
                }
                case 5 -> {
                    configuredOrderItems.removeIf(oi -> oi.getItemId().equals(target.getProduct().getId()));
                    configuredOrderItems.add(currentDecorator);
                    System.out.println("Successfully saved decorated order item for checkout.");
                    decorating = false;
                }
                case 0 -> decorating = false;
            }
        }
    }

    private void handleCheckout() {
        ConsoleUiHelper.printHeader("Checkout Processing - Facade, Strategy & Adapter Patterns");
        if (shoppingCart.isEmpty()) {
            System.out.println("Your cart is empty. Please add items to cart before proceeding to checkout.");
            return;
        }

        shoppingCart.printCartSummary();

        // 1. Select Discount Strategy
        ConsoleUiHelper.printSubHeader("Step 1: Choose Discount Strategy");
        System.out.println("  1. Customer Loyalty Tier (GOLD Member: 12% off)");
        System.out.println("  2. Promotional Coupon Code [FALL15] (15% off cart)");
        System.out.println("  3. Flat Order Coupon [SAVE50] ($50 off over $250)");
        System.out.println("  4. Buy-One-Get-One-Free (BOGO) paired units");
        System.out.println("  5. No Promotional Discount");

        int stratChoice = ConsoleUiHelper.promptInt("Select discount strategy", 1, 5);
        DiscountStrategy strategy = switch (stratChoice) {
            case 1 -> new TieredLoyaltyDiscountStrategy();
            case 2 -> new PercentageDiscountStrategy("FALL15", 15.0);
            case 3 -> new FlatRateDiscountStrategy("SAVE50", 50.0, 250.0);
            case 4 -> new BuyOneGetOneDiscountStrategy("ALL");
            default -> new NoDiscountStrategy();
        };

        // 2. Select Payment Gateway Adapter
        ConsoleUiHelper.printSubHeader("Step 2: Choose Payment Gateway (Adapter)");
        System.out.println("  1. Stripe Payment Gateway (Credit/Debit Card via API Adapter)");
        System.out.println("  2. PayPal Express Checkout (Token Authorization Adapter)");
        System.out.println("  3. Web3 USDT Settlement Gateway (Decentralized Smart Contract)");

        int payChoice = ConsoleUiHelper.promptInt("Select payment provider", 1, 3);
        PaymentGateway selectedGateway = paymentGateways.get(payChoice);

        // 3. Confirm & Execute via CheckoutFacade
        boolean confirm = ConsoleUiHelper.promptBoolean("Proceed to execute unified checkout transaction?");
        if (!confirm) {
            System.out.println("Checkout cancelled by customer.");
            return;
        }

        try {
            Order completedOrder = checkoutFacade.processCheckout(
                    shoppingCart,
                    currentCustomer,
                    strategy,
                    selectedGateway,
                    configuredOrderItems
            );
            orderHistory.add(completedOrder);
            configuredOrderItems.clear();
            System.out.println("Order status is now PAID and entered into logistics dispatch queue.");
        } catch (Exception ex) {
            System.err.println("Checkout transaction failed: " + ex.getMessage());
        }
    }

    private void handleViewObserverStream() {
        ConsoleUiHelper.printHeader("Observer Pattern - Real-Time Event Dispatch Monitor");
        System.out.println("Active Observers attached to InventorySubject:");
        System.out.println("  1. CustomerAlertObserver  [Recipient: alex@example.com / nodir@university.edu]");
        System.out.println("  2. WarehouseInventoryObserver [Logistics Hub: TASHKENT-CENTRAL]");
        System.out.println("  3. AnalyticsAuditObserver [System Audit Trail]");
        ConsoleUiHelper.printThinDivider();

        System.out.println("Simulate Live Store State Mutations:");
        System.out.println("  1. Trigger Price Drop Alert (broadcast to customers)");
        System.out.println("  2. Trigger Low-Stock Depletion (dispatch warehouse replenishment PO)");
        System.out.println("  3. Restock Product (broadcast back-in-stock notifications)");
        System.out.println("  0. Back to Main Menu");

        int choice = ConsoleUiHelper.promptInt("Select action", 0, 3);
        switch (choice) {
            case 1 -> {
                String id = "EL-101";
                BaseProduct p = inventorySubject.getProduct(id);
                double currentPrice = p.getPrice();
                double dropPrice = Math.round(currentPrice * 0.85 * 100.0) / 100.0;
                System.out.printf("Dropping %s price from $%.2f to $%.2f...%n", p.getName(), currentPrice, dropPrice);
                inventorySubject.updateProductPrice(id, dropPrice);
            }
            case 2 -> {
                String id = "EL-102";
                BaseProduct p = inventorySubject.getProduct(id);
                System.out.printf("Depleting stock for %s to 2 units (triggers low stock PO)...%n", p.getName());
                inventorySubject.updateStock(id, 2);
            }
            case 3 -> {
                String id = "EL-102";
                BaseProduct p = inventorySubject.getProduct(id);
                System.out.printf("Restocking %s with 15 fresh units...%n", p.getName());
                inventorySubject.updateStock(id, 15);
            }
        }
    }

    private void handleViewAuditLog() {
        ConsoleUiHelper.printHeader("Analytics & Audit Trail - Observer Event Log");
        List<EcommerceEvent> log = auditObserver.getAuditLog();
        if (log.isEmpty()) {
            System.out.println("No store events recorded in this session yet.");
        } else {
            System.out.printf("Total Recorded Audit Events: %d%n", log.size());
            ConsoleUiHelper.printThinDivider();
            for (int i = 0; i < log.size(); i++) {
                System.out.printf("%2d. %s%n", i + 1, log.get(i));
            }
        }
    }

    public static void main(String[] args) {
        EcommerceApp app = new EcommerceApp();
        app.run();
    }
}
