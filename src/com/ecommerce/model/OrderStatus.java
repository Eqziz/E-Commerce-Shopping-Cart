package com.ecommerce.model;

/**
 * Lifecycle states of an e-commerce order.
 * 
 * @author AinurSarsenbai
 */
public enum OrderStatus {
    PENDING("Awaiting Payment Authorization"),
    PAID("Payment Confirmed, In Warehouse Queue"),
    PROCESSING("Items Being Picked and Decorated"),
    SHIPPED("Dispatched with Courier"),
    DELIVERED("Delivered to Recipient"),
    CANCELLED("Order Cancelled and Stock Restored");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
