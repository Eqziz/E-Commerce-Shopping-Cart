package com.ecommerce.decorator;

/**
 * Concrete Decorator in the Decorator Pattern.
 * Adds 2-year extended warranty coverage and replacement guarantee to eligible items.
 * 
 * @author baktiyarovamereilim-lang
 */
public class ExtendedWarrantyDecorator extends OrderItemDecorator {
    private final int warrantyMonths;
    private final double warrantyRate;

    public ExtendedWarrantyDecorator(OrderItem wrappedItem, int warrantyMonths, double warrantyRate) {
        super(wrappedItem);
        this.warrantyMonths = Math.max(12, warrantyMonths);
        this.warrantyRate = (warrantyRate > 0.0 && warrantyRate <= 1.0) ? warrantyRate : 0.10; // default 10%
    }

    public ExtendedWarrantyDecorator(OrderItem wrappedItem) {
        this(wrappedItem, 24, 0.10);
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public double getWarrantyCostPerUnit() {
        return Math.max(14.99, Math.round(getPrice() * warrantyRate * 100.0) / 100.0);
    }

    @Override
    public double getTotalPrice() {
        double addedFee = getWarrantyCostPerUnit() * getQuantity();
        return Math.round((super.getTotalPrice() + addedFee) * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        return String.format("%s + [%d-Mo Extended Warranty (+$%.2f/ea)]",
                super.getDescription(), warrantyMonths, getWarrantyCostPerUnit());
    }
}
