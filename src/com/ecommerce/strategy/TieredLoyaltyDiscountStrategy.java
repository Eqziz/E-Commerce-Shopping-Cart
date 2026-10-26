package com.ecommerce.strategy;

import com.ecommerce.model.CartItem;

import java.util.List;

/**
 * Concrete Strategy in the Strategy Pattern.
 * Evaluates the customer membership tier (BRONZE, SILVER, GOLD, PLATINUM)
 * and grants escalating tier-based savings.
 * 
 * @author Eqziz
 */
public class TieredLoyaltyDiscountStrategy implements DiscountStrategy {

    @Override
    public String getStrategyName() {
        return "Customer Loyalty Tier Discount";
    }

    @Override
    public DiscountResult applyDiscount(double subtotal, List<CartItem> items, String customerTier) {
        if (customerTier == null) {
            customerTier = "STANDARD";
        }

        double rate = switch (customerTier.toUpperCase()) {
            case "PLATINUM" -> 0.20; // 20%
            case "GOLD" -> 0.12;     // 12%
            case "SILVER" -> 0.07;   // 7%
            case "BRONZE" -> 0.03;   // 3%
            default -> 0.00;
        };

        double discount = Math.round((subtotal * rate) * 100.0) / 100.0;
        String desc = rate > 0.0
                ? String.format("Tier %s membership discount of %.0f%% applied", customerTier.toUpperCase(), rate * 100.0)
                : String.format("Tier %s does not qualify for loyalty discounts", customerTier);

        return new DiscountResult(String.format("Loyalty [%s]", customerTier.toUpperCase()), discount, subtotal, desc);
    }
}
