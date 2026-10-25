package com.ecommerce.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Observer in the Observer Pattern.
 * Simulates warehouse operations staff listening for low stock and stock depletion events.
 * 
 * @author baktiyarovamereilim-lang
 */
public class WarehouseInventoryObserver implements Observer {
    private final String warehouseCode;
    private final List<EcommerceEvent> restockTickets = new ArrayList<>();

    public WarehouseInventoryObserver(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    @Override
    public String getObserverId() {
        return "WH-" + warehouseCode;
    }

    @Override
    public String getObserverName() {
        return "Warehouse Logistics Hub [" + warehouseCode + "]";
    }

    @Override
    public void update(EcommerceEvent event) {
        if (event.getType() == EventType.LOW_STOCK_WARNING || event.getType() == EventType.OUT_OF_STOCK) {
            restockTickets.add(event);
            System.out.printf("[Warehouse %s Procurement]: Auto-dispatch restock PO for %s (#%s) -> Current level: %.0f units%n",
                    warehouseCode, event.getProductName(), event.getProductId(), event.getNewValue());
        }
    }

    public List<EcommerceEvent> getRestockTickets() {
        return Collections.unmodifiableList(restockTickets);
    }
}
