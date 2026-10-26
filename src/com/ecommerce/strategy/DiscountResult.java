package com.ecommerce.strategy;

/**
 * Encapsulates the financial outcome of applying a discount strategy.
 * 
 * @author Eqziz
 */
public class DiscountResult {
    private final String strategyName;
    private final double discountAmount;
    private final double discountedTotal;
    private final String description;

    public DiscountResult(String strategyName, double discountAmount, double originalTotal, String description) {
        if (discountAmount < 0.0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        this.strategyName = strategyName != null ? strategyName : "Standard Pricing";
        this.discountAmount = Math.min(discountAmount, originalTotal);
        this.discountedTotal = Math.round((originalTotal - this.discountAmount) * 100.0) / 100.0;
        this.description = description != null ? description : "";
    }

    public String getStrategyName() {
        return strategyName;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getDiscountedTotal() {
        return discountedTotal;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("[%s] Savings: $%.2f | Final Total: $%.2f (%s)",
                strategyName, discountAmount, discountedTotal, description);
    }
}
