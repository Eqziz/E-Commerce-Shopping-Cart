package com.ecommerce.decorator;

/**
 * Component interface in the Decorator Pattern.
 * Defines standard operations for order line items that can be dynamically decorated
 * with value-added services such as warranty, gift wrapping, and express shipping.
 * 
 * @author baktiyarovamereilim-lang
 */
public interface OrderItem {
    String getItemId();
    String getName();
    double getPrice();
    int getQuantity();
    double getTotalPrice();
    String getDescription();
}
