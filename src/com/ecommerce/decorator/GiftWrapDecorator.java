package com.ecommerce.decorator;

/**
 * Concrete Decorator in the Decorator Pattern.
 * Adds decorative gift packaging and custom message card services to an order line item.
 * 
 * @author baktiyarovamereilim-lang
 */
public class GiftWrapDecorator extends OrderItemDecorator {
    private final String greetingMessage;
    private final double wrapCostPerUnit;

    public GiftWrapDecorator(OrderItem wrappedItem, String greetingMessage, double wrapCostPerUnit) {
        super(wrappedItem);
        this.greetingMessage = greetingMessage != null ? greetingMessage : "Best Wishes!";
        this.wrapCostPerUnit = Math.max(0.0, wrapCostPerUnit);
    }

    public GiftWrapDecorator(OrderItem wrappedItem, String greetingMessage) {
        this(wrappedItem, greetingMessage, 4.99);
    }

    public String getGreetingMessage() {
        return greetingMessage;
    }

    public double getWrapCostPerUnit() {
        return wrapCostPerUnit;
    }

    @Override
    public double getTotalPrice() {
        double addedFee = wrapCostPerUnit * getQuantity();
        return Math.round((super.getTotalPrice() + addedFee) * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        return String.format("%s + [Gift Wrap: \"%s\" (+$%.2f/ea)]",
                super.getDescription(), greetingMessage, wrapCostPerUnit);
    }
}
