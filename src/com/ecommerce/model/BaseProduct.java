package com.ecommerce.model;

/**
 * Base interface representing a product contract across the store.
 * Defines standard attributes required by catalog items, custom builders, and cart items.
 * 
 * @author Eqziz
 */
public interface BaseProduct {
    String getId();
    String getName();
    String getDescription();
    double getPrice();
    ProductType getProductType();
    boolean isAvailable();
    void setPrice(double newPrice);
    void setAvailable(boolean available);
}
