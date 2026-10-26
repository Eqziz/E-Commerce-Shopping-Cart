package com.ecommerce.strategy;

import com.ecommerce.model.CartItem;

import java.util.List;

/**
 * Concrete Strategy in the Strategy Pattern.
 * Applies a percentage-based discount across the overall cart subtotal.
 * 
 * @author Eqziz
 */
public class PercentageDiscountStrategy implements DiscountStrategy {
    private final double percentage;
    private final String promoCode;

    public PercentageDiscountStrategy(String promoCode, double percentage) {
        if (percentage <= 0.0 || percentage > 100.0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        this.promoCode = promoCode != null ? promoCode : "PERCENT";
        this.percentage = percentage;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getPromoCode() {
        return promoCode;
    }

    @Override
    public String getStrategyName() {
        return String.format("Promo [%s] (%.0f%% Off)", promoCode, percentage);
    }

    @Override
    public DiscountResult applyDiscount(double subtotal, List<CartItem> items, String customerTier) {
        double discount = Math.round((subtotal * (percentage / 100.0)) * 100.0) / 100.0;
        return new DiscountResult(getStrategyName(), discount, subtotal,
                String.format("Applied %.0f%% promotional discount via code %s", percentage, promoCode));
    }
}
