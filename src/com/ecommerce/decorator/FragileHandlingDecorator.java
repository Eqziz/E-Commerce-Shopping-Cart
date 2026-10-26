package com.ecommerce.decorator;

/**
 * Concrete Decorator in the Decorator Pattern.
 * Adds specialized shock-absorbing protective packaging and delicate cargo insurance.
 * 
 * @author baktiyarovamereilim-lang
 */
public class FragileHandlingDecorator extends OrderItemDecorator {
    private final double handlingFee;

    public FragileHandlingDecorator(OrderItem wrappedItem, double handlingFee) {
        super(wrappedItem);
        this.handlingFee = Math.max(0.0, handlingFee);
    }

    public FragileHandlingDecorator(OrderItem wrappedItem) {
        this(wrappedItem, 7.50);
    }

    public double getHandlingFee() {
        return handlingFee;
    }

    @Override
    public double getTotalPrice() {
        return Math.round((super.getTotalPrice() + handlingFee) * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        return String.format("%s + [Fragile Shock-Proof Packaging (+$%.2f)]",
                super.getDescription(), handlingFee);
    }
}
