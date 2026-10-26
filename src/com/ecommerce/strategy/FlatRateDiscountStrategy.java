package com.ecommerce.strategy;

import com.ecommerce.model.CartItem;

import java.util.List;

/**
 * Concrete Strategy in the Strategy Pattern.
 * Deducts a fixed monetary amount when the cart meets a qualifying minimum order value.
 * 
 * @author Eqziz
 */
public class FlatRateDiscountStrategy implements DiscountStrategy {
    private final String promoCode;
    private final double flatAmount;
    private final double minimumSpend;

    public FlatRateDiscountStrategy(String promoCode, double flatAmount, double minimumSpend) {
        if (flatAmount <= 0.0) {
            throw new IllegalArgumentException("Flat amount must be positive");
        }
        this.promoCode = promoCode != null ? promoCode : "FLAT";
        this.flatAmount = flatAmount;
        this.minimumSpend = Math.max(0.0, minimumSpend);
    }

    @Override
    public String getStrategyName() {
        return String.format("Flat Discount [%s] ($%.2f Off)", promoCode, flatAmount);
    }

    @Override
    public DiscountResult applyDiscount(double subtotal, List<CartItem> items, String customerTier) {
        if (subtotal < minimumSpend) {
            return new DiscountResult(getStrategyName(), 0.0, subtotal,
                    String.format("Minimum spend of $%.2f not met (Cart: $%.2f)", minimumSpend, subtotal));
        }
        double actualDiscount = Math.min(flatAmount, subtotal);
        return new DiscountResult(getStrategyName(), actualDiscount, subtotal,
                String.format("Deducted $%.2f flat discount (Qualifying spend: $%.2f)", actualDiscount, minimumSpend));
    }
}
