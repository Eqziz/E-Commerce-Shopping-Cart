package com.ecommerce.decorator;

/**
 * Concrete Decorator in the Decorator Pattern.
 * Adds priority air express shipping and tracking assurance to the order item.
 * 
 * @author baktiyarovamereilim-lang
 */
public class ExpressShippingDecorator extends OrderItemDecorator {
    private final double expressFee;

    public ExpressShippingDecorator(OrderItem wrappedItem, double expressFee) {
        super(wrappedItem);
        this.expressFee = Math.max(0.0, expressFee);
    }

    public ExpressShippingDecorator(OrderItem wrappedItem) {
        this(wrappedItem, 12.50);
    }

    public double getExpressFee() {
        return expressFee;
    }

    @Override
    public double getTotalPrice() {
        return Math.round((super.getTotalPrice() + expressFee) * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        return String.format("%s + [Express Air Shipping (+$%.2f)]",
                super.getDescription(), expressFee);
    }
}
