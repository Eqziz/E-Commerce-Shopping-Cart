package com.ecommerce.demo;

import com.ecommerce.builder.CustomProduct;
import com.ecommerce.builder.CustomProductBuilder;
import com.ecommerce.builder.ProductDirector;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.factory.ProductCatalogRegistry;
import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.ProductType;
import com.ecommerce.observer.AnalyticsAuditObserver;
import com.ecommerce.observer.CustomerAlertObserver;
import com.ecommerce.observer.InventorySubject;
import com.ecommerce.observer.WarehouseInventoryObserver;

import java.util.Map;

/**
 * Verification test suite and runnable milestone demonstration for Phase 1.
 * Tests the collaboration between:
 * 1. Factory Method Pattern (ProductCatalogRegistry & category factories)
 * 2. Builder Pattern (CustomProductBuilder & ProductDirector presets)
 * 3. Observer Pattern (InventorySubject, Customer alerts, Warehouse reorders, BI audit)
 * 4. ShoppingCart container operations
 * 
 * @author Eqziz (Team Lead)
 */
public class MilestoneOneDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("E-COMMERCE SHOPPING CART PLATFORM - MILESTONE 1 VERIFICATION");
        System.out.println("Team: Eqziz (Lead), baktiyarovamereilim-lang, AinurSarsenbai");
        System.out.println("================================================================================\n");

        // 1. Factory Method Pattern (AinurSarsenbai)
        System.out.println("[1/4] Factory Method Pattern: Catalog Production");
        System.out.println("--------------------------------------------------------------------------------");
        ProductCatalogRegistry catalog = new ProductCatalogRegistry();

        BaseProduct phone = catalog.createProduct(ProductType.ELECTRONICS, "EL-001",
                "UltraPhone 15 Pro", "Flagship smartphone with OLED display", 999.99,
                Map.of("weightKg", 0.22, "dimensions", "15x7x0.8 cm"));

        BaseProduct hoodie = catalog.createProduct(ProductType.CLOTHING, "CL-104",
                "Cyberpunk Hoodie", "Organic cotton streetwear hoodie", 79.50,
                Map.of("size", "L", "weightKg", 0.55));

        BaseProduct sdpBook = catalog.createProduct(ProductType.BOOK, "BK-501",
                "Design Patterns in Java", "Comprehensive GoF guide with modern examples", 49.99,
                Map.of("author", "Erich Gamma et al.", "isbn", "978-0201633610", "weightKg", 0.8));

        BaseProduct ideLicense = catalog.createProduct(ProductType.DIGITAL, "DG-902",
                "IntelliJ IDEA Ultimate", "1-Year Commercial Development Subscription", 169.00,
                Map.of("downloadUrl", "https://jetbrains.com/download", "licenseFormat", "COMM-2026-ACTIVE"));

        System.out.println("Result: 4 polymorphic products manufactured via Factory Method.\n");

        // 2. Builder Pattern (Eqziz)
        System.out.println("[2/4] Builder Pattern: Customizable Product Configurations");
        System.out.println("--------------------------------------------------------------------------------");
        ProductDirector director = new ProductDirector();
        CustomProductBuilder builder = new CustomProductBuilder();

        // Preset configuration via Director
        CustomProduct gamingPc = director.constructGamingPc(builder, "CUST-PC-01");
        System.out.println("Preset configuration created via ProductDirector:");
        System.out.println(gamingPc);

        // Custom configuration step-by-step
        CustomProduct bespokeAudioStation = builder.reset()
                .setId("CUST-AUDIO-99")
                .setName("SoundMaster Studio Rig")
                .setDescription("Custom audio production station with studio monitors")
                .setBasePrice(800.00)
                .setProcessor("Apple M3 Max 16-Core", 700.00)
                .setMemoryRam("64GB Unified Memory", 400.00)
                .setStorage("4TB High-Speed SSD", 350.00)
                .setPackagingType("Heavy-duty Flight Case", 120.00)
                .addAccessory("Reference Studio Monitor Headphones", 199.00)
                .addAccessory("USB-C Audio Interface 24-bit/192kHz", 149.00)
                .build();
        System.out.println("\nCustom configuration assembled via step-by-step Builder chaining:");
        System.out.println(bespokeAudioStation);
        System.out.println("Result: 2 custom configurations assembled via Builder.\n");

        // 3. Observer Pattern (baktiyarovamereilim-lang)
        System.out.println("[3/4] Observer Pattern: Real-Time Stock and Price Alerts");
        System.out.println("--------------------------------------------------------------------------------");
        InventorySubject inventorySubject = new InventorySubject();

        CustomerAlertObserver alice = new CustomerAlertObserver("CUST-A", "Alice Smith", "alice@example.com");
        alice.subscribeToProduct(phone.getId());

        WarehouseInventoryObserver warehouseEast = new WarehouseInventoryObserver("DISTRIBUTION-EAST");
        AnalyticsAuditObserver auditSystem = new AnalyticsAuditObserver();

        inventorySubject.attach(alice);
        inventorySubject.attach(warehouseEast);
        inventorySubject.attach(auditSystem);

        inventorySubject.registerProduct(phone, 10);
        inventorySubject.registerProduct(gamingPc, 4);

        System.out.println("\n-- Event 1: Low Stock Warning --");
        inventorySubject.updateStock(gamingPc.getId(), 2);

        System.out.println("\n-- Event 2: Out of Stock Warning --");
        inventorySubject.updateStock(gamingPc.getId(), 0);

        System.out.println("\n-- Event 3: Price Drop Alert --");
        inventorySubject.updateProductPrice(phone.getId(), 849.99);

        System.out.printf("Audit events logged: %d%n", auditSystem.getAuditLog().size());
        System.out.printf("Customer alerts received: %d%n", alice.getReceivedNotifications().size());
        System.out.printf("Warehouse tickets dispatched: %d%n\n", warehouseEast.getRestockTickets().size());

        // 4. Shopping Cart Integration
        System.out.println("[4/4] Shopping Cart Aggregation");
        System.out.println("--------------------------------------------------------------------------------");
        ShoppingCart cart = new ShoppingCart("CART-2026-X1", "CUST-A");
        cart.addItem(phone, 1);
        cart.addItem(hoodie, 2);
        cart.addItem(sdpBook, 1);
        cart.addItem(ideLicense, 1);
        cart.addItem(gamingPc, 1);

        cart.printCartSummary();

        System.out.println("Updating cart: hoodie quantity set to 3, book removed.");
        cart.updateQuantity(hoodie.getId(), 3);
        cart.removeItem(sdpBook.getId());

        cart.printCartSummary();

        System.out.println("\n================================================================================");
        System.out.println("Milestone 1 execution completed successfully.");
        System.out.println("================================================================================");
    }
}
