package com.ecommerce.decorator;

/**
 * Base Decorator in the Decorator Pattern.
 * Holds a reference to a wrapped {@link OrderItem} and forwards default invocations.
 * Subclasses override methods to augment behavior and accumulate costs.
 * 
 * @author baktiyarovamereilim-lang
 */
public abstract class OrderItemDecorator implements OrderItem {
    protected final OrderItem wrappedItem;

    public OrderItemDecorator(OrderItem wrappedItem) {
        if (wrappedItem == null) {
            throw new IllegalArgumentException("Wrapped order item cannot be null");
        }
        this.wrappedItem = wrappedItem;
    }

    @Override
    public String getItemId() {
        return wrappedItem.getItemId();
    }

    @Override
    public String getName() {
        return wrappedItem.getName();
    }

    @Override
    public double getPrice() {
        return wrappedItem.getPrice();
    }

    @Override
    public int getQuantity() {
        return wrappedItem.getQuantity();
    }

    @Override
    public double getTotalPrice() {
        return wrappedItem.getTotalPrice();
    }

    @Override
    public String getDescription() {
        return wrappedItem.getDescription();
    }

    @Override
    public String toString() {
        return String.format("%s [Total: $%.2f]", getDescription(), getTotalPrice());
    }
}
