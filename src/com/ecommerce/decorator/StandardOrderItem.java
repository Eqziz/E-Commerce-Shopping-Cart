package com.ecommerce.decorator;

import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.CartItem;

/**
 * Concrete Component in the Decorator Pattern.
 * Represents an undecorated base line item in an order.
 * 
 * @author baktiyarovamereilim-lang
 */
public class StandardOrderItem implements OrderItem {
    private final BaseProduct product;
    private final int quantity;

    public StandardOrderItem(BaseProduct product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.product = product;
        this.quantity = quantity;
    }

    public StandardOrderItem(CartItem cartItem) {
        this(cartItem.getProduct(), cartItem.getQuantity());
    }

    public BaseProduct getProduct() {
        return product;
    }

    @Override
    public String getItemId() {
        return product.getId();
    }

    @Override
    public String getName() {
        return product.getName();
    }

    @Override
    public double getPrice() {
        return product.getPrice();
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public double getTotalPrice() {
        return Math.round(product.getPrice() * quantity * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        return product.getDescription();
    }

    @Override
    public String toString() {
        return String.format("%s (x%d) @ $%.2f each = $%.2f",
                getName(), getQuantity(), getPrice(), getTotalPrice());
    }
}
