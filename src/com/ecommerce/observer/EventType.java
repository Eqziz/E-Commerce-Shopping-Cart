package com.ecommerce.observer;

/**
 * Event classifications broadcast by publishers in the Observer pattern.
 * 
 * @author baktiyarovamereilim-lang
 */
public enum EventType {
    PRICE_CHANGED("Product Price Update"),
    PRICE_DROPPED("Price Drop Alert"),
    STOCK_UPDATED("Inventory Stock Update"),
    LOW_STOCK_WARNING("Low Stock Warning"),
    OUT_OF_STOCK("Out of Stock Alert"),
    RESTOCKED("Inventory Restocked Alert"),
    ORDER_STATUS_CHANGED("Order Status Change");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
