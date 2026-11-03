package com.ecommerce.test;

import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.PhysicalProduct;
import com.ecommerce.model.ProductType;
import com.ecommerce.observer.AnalyticsAuditObserver;
import com.ecommerce.observer.CustomerAlertObserver;
import com.ecommerce.observer.EventType;
import com.ecommerce.observer.InventorySubject;
import com.ecommerce.observer.WarehouseInventoryObserver;

/**
 * Unit tests validating Observer pattern implementation.
 * 
 * @author baktiyarovamereilim-lang
 */
public class ObserverPatternTest {

    public static void runTests() {
        System.out.println("Running ObserverPatternTest...");
        InventorySubject inventory = new InventorySubject();
        CustomerAlertObserver customer = new CustomerAlertObserver("C-1", "Test Customer", "test@test.com");
        WarehouseInventoryObserver warehouse = new WarehouseInventoryObserver("WH-1");
        AnalyticsAuditObserver audit = new AnalyticsAuditObserver();

        inventory.attach(customer);
        inventory.attach(warehouse);
        inventory.attach(audit);

        BaseProduct prod = new PhysicalProduct("P-1", "Test Item", "Test Desc", 100.0, ProductType.ELECTRONICS, 1.0, "10x10");
        inventory.registerProduct(prod, 10);
        customer.subscribeToProduct(prod.getId());

        // Test 1: Low stock trigger
        inventory.updateStock(prod.getId(), 2);
        SimpleAssert.assertTrue(warehouse.getRestockTickets().size() >= 1, "Warehouse must receive low stock ticket");

        // Test 2: Price drop alert
        inventory.updateProductPrice(prod.getId(), 75.0);
        SimpleAssert.assertTrue(customer.getReceivedNotifications().size() >= 1, "Customer must receive deal alert on price drop");
        SimpleAssert.assertTrue(audit.countEventsByType(EventType.PRICE_DROPPED) >= 1, "Audit must record price drop event");

        System.out.println("  [PASSED] ObserverPatternTest passed.");
    }
}
